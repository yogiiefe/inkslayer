package com.yogiiefe.inkslayer.recognition;

import java.util.ArrayList;
import java.util.List;

/** Mouse strokes in canvas-local Y-down coordinates. Bounded, independent of libGDX. */
public final class Drawing {
    private final List<List<InkPoint>> strokes = new ArrayList<>();
    private List<InkPoint> active;
    public void begin(double x, double y) {
        cancelActive();
        if (strokes.size() >= KanjiRecognizer.MAX_STROKES) return;
        active = new ArrayList<>(); active.add(new InkPoint(x, y));
    }
    public void add(double x, double y) {
        if (active == null || active.size() >= 2048) return;
        var point = new InkPoint(x, y);
        if (active.getLast().distance(point) >= 0.7) active.add(point);
    }
    public void end() { if (active != null) { strokes.add(List.copyOf(active)); active = null; } }
    public void cancelActive() { active = null; }
    public void undo() { if (active != null) active = null; else if (!strokes.isEmpty()) strokes.removeLast(); }
    public void clear() { strokes.clear(); active = null; }
    public int count() { return strokes.size(); }
    public boolean isActive() { return active != null; }
    public List<List<InkPoint>> snapshot() { return List.copyOf(strokes); }
    public List<InkPoint> active() { return active == null ? List.of() : active; }
}
