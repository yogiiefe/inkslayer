package com.yogiiefe.inkslayer.recognition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Small-vocabulary deterministic geometry, not a calibrated literacy assessment. */
public final class KanjiRecognizer {
    public record Result(boolean accepted, double distance, boolean orderCorrect, boolean directionCorrect, String message) {}
    public record Match(String id, Result result) {}
    private record Score(String id, double mean, double worst, boolean ordered, boolean directed) {}

    static final int MAX_STROKES = 12;
    private static final int N = 32;
    private static final double MINIMUM_SPAN = 12;
    private static final double AMBIGUITY_MARGIN = 0.025;
    private static final double DIRECTION_MARGIN = 0.003;
    private final Map<String, List<List<InkPoint>>> templates;
    private final double threshold;

    public KanjiRecognizer(List<List<InkPoint>> strokes, double threshold) {
        this(Map.of("水", strokes), threshold);
    }

    public static KanjiRecognizer fromTemplates(Map<String, List<List<InkPoint>>> templates, double threshold) {
        return new KanjiRecognizer(templates, threshold);
    }

    private KanjiRecognizer(Map<String, List<List<InkPoint>>> source, double threshold) {
        if (source.isEmpty() || threshold <= 0 || !Double.isFinite(threshold)) {
            throw new IllegalArgumentException("Invalid template collection or threshold");
        }
        this.threshold = threshold;
        this.templates = new LinkedHashMap<>();
        new TreeMap<>(source).forEach((id, strokes) -> templates.put(id, normalize(strokes)));
    }

    public Result recognize(List<List<InkPoint>> input, boolean strict) {
        return match(input, strict, templates.keySet()).result();
    }

    public Match match(List<List<InkPoint>> input, boolean strict, Set<String> eligibleIds) {
        if (eligibleIds.isEmpty()) return rejected("No Kanji is available for this action.");
        if (input.isEmpty()) return rejected("Draw a Kanji in the canvas.");

        List<List<InkPoint>> sample;
        try {
            sample = normalize(input, MINIMUM_SPAN);
        } catch (IllegalArgumentException ex) {
            return rejected(ex.getMessage());
        }

        Score best = null;
        Score runnerUp = null;
        for (var entry : templates.entrySet()) {
            var template = entry.getValue();
            if (template.size() != sample.size()) continue;
            var score = score(entry.getKey(), sample, template);
            if (score.worst() > threshold * 1.65) continue;
            if (best == null || score.mean() < best.mean()) {
                runnerUp = best;
                best = score;
            } else if (runnerUp == null || score.mean() < runnerUp.mean()) {
                runnerUp = score;
            }
        }

        if (best == null || best.mean() > threshold) {
            if (eligibleIds.size() == 1) {
                String expected = eligibleIds.iterator().next();
                var template = templates.get(expected);
                if (template != null && input.size() != template.size()) {
                    return rejected(expected + " uses " + template.size() + " strokes. Undo or add a stroke.");
                }
            }
            return rejected("Shape not recognized. Compare your strokes with the guide.");
        }
        if (runnerUp != null && runnerUp.mean() - best.mean() < AMBIGUITY_MARGIN) {
            return rejected("Two characters look similar. Check stroke shape and placement.");
        }
        if (!eligibleIds.contains(best.id())) {
            return new Match(null, new Result(false, best.mean(), best.ordered(), best.directed(),
                best.id() + " is not available for this action."));
        }

        boolean accepted = !strict || (best.ordered() && best.directed());
        String message = best.id() + " recognized.";
        if (!best.ordered()) message += " Follow the numbered stroke order.";
        else if (!best.directed()) message += " Check the stroke directions.";
        return new Match(accepted ? best.id() : null,
            new Result(accepted, best.mean(), best.ordered(), best.directed(), message));
    }

    private static Match rejected(String message) {
        return new Match(null, new Result(false, 1, false, false, message));
    }

    private static Score score(String id, List<List<InkPoint>> sample, List<List<InkPoint>> template) {
        int size = sample.size();
        double[][] costs = new double[size][size];
        boolean[][] correctDirection = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double forward = distance(sample.get(i), template.get(j), false);
                double reverse = distance(sample.get(i), template.get(j), true);
                costs[i][j] = Math.min(forward, reverse);
                correctDirection[i][j] = reverse - forward >= DIRECTION_MARGIN;
            }
        }

        // Exact stroke assignment. Twelve strokes bound this to 4096 subsets.
        int complete = (1 << size) - 1;
        double[] best = new double[complete + 1];
        int[] chosen = new int[complete + 1];
        Arrays.fill(best, Double.POSITIVE_INFINITY);
        best[0] = 0;
        for (int mask = 0; mask < complete; mask++) {
            int i = Integer.bitCount(mask);
            for (int j = 0; j < size; j++) {
                int bit = 1 << j;
                if ((mask & bit) != 0) continue;
                int next = mask | bit;
                double cost = best[mask] + costs[i][j];
                if (cost < best[next]) {
                    best[next] = cost;
                    chosen[next] = j;
                }
            }
        }
        boolean ordered = true;
        boolean directed = true;
        double worst = 0;
        for (int mask = complete; mask != 0; ) {
            int i = Integer.bitCount(mask) - 1;
            int j = chosen[mask];
            ordered &= i == j;
            directed &= correctDirection[i][j];
            worst = Math.max(worst, costs[i][j]);
            mask ^= 1 << j;
        }
        return new Score(id, best[complete] / size, worst, ordered, directed);
    }

    public static List<List<InkPoint>> normalize(List<List<InkPoint>> strokes) {
        return normalize(strokes, 0.00001);
    }

    private static List<List<InkPoint>> normalize(List<List<InkPoint>> strokes, double minimumSpan) {
        double minX = Double.POSITIVE_INFINITY, minY = minX, maxX = -minX, maxY = -minX;
        int total = 0;
        if (strokes.isEmpty() || strokes.size() > MAX_STROKES) {
            throw new IllegalArgumentException("Use between one and twelve strokes.");
        }
        for (var stroke : strokes) {
            if (stroke.size() < 2 || stroke.size() > 2048) {
                throw new IllegalArgumentException("Undo any taps and draw clear strokes.");
            }
            for (var p : stroke) {
                if (!Double.isFinite(p.x()) || !Double.isFinite(p.y())) {
                    throw new IllegalArgumentException("Clear the canvas and draw again.");
                }
                minX = Math.min(minX,p.x()); minY = Math.min(minY,p.y()); maxX = Math.max(maxX,p.x()); maxY = Math.max(maxY,p.y()); total++;
            }
        }
        double size = Math.max(maxX-minX,maxY-minY);
        if (!Double.isFinite(size) || total > 8192) {
            throw new IllegalArgumentException("Clear the canvas and draw again.");
        }
        if (size < minimumSpan) {
            throw new IllegalArgumentException("Make the character larger so its strokes are clear.");
        }
        var result = new ArrayList<List<InkPoint>>();
        double marginX = (1 - (maxX - minX) / size) / 2;
        double marginY = (1 - (maxY - minY) / size) / 2;
        for (var stroke : strokes) {
            var normalized = new ArrayList<InkPoint>();
            for (var p : stroke) normalized.add(new InkPoint((p.x()-minX)/size+marginX,(p.y()-minY)/size+marginY));
            result.add(resample(normalized));
        }
        return List.copyOf(result);
    }
    private static List<InkPoint> resample(List<InkPoint> points) {
        double[] cumulative = new double[points.size()];
        for (int i=1;i<points.size();i++) cumulative[i]=cumulative[i-1]+points.get(i).distance(points.get(i-1));
        double length=cumulative[cumulative.length-1];
        if (length < 0.005) throw new IllegalArgumentException("Undo the tiny mark and redraw that stroke.");
        var result=new ArrayList<InkPoint>(); int segment=1;
        for(int i=0;i<N;i++) {
            double target=length*i/(N-1);
            while(segment<cumulative.length-1 && cumulative[segment]<target) segment++;
            double span=cumulative[segment]-cumulative[segment-1];
            double t=span==0?0:(target-cumulative[segment-1])/span;
            var a=points.get(segment-1); var b=points.get(segment);
            result.add(new InkPoint(a.x()+(b.x()-a.x())*t,a.y()+(b.y()-a.y())*t));
        }
        return List.copyOf(result);
    }
    private static double distance(List<InkPoint> a,List<InkPoint> b,boolean reverse) {
        double sum=0; for(int i=0;i<N;i++) sum+=a.get(i).distance(b.get(reverse?N-1-i:i)); return sum/N;
    }
}
