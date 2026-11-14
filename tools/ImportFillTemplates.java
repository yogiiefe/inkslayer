import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

class ImportFillTemplates {
    private static final String TEMP = "build/fill-kanji/";
    private static final String REVISION = "d95a976";
    private static final String GLYPHS = "水火木土金石山川海鉄犬猫鳥魚馬兎毛羽亀尾雨風雪雲空晴雷寒涼暑米肉卵豆塩茶麦芋甘";
    private static final Pattern PATH = Pattern.compile("<path id=\"kvg:([0-9a-f]+)-s(\\d+)\"[^>]* d=\"([^\"]+)\"");
    private static final Pattern TOKEN = Pattern.compile("[A-Za-z]|[-+]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)");

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        var revisionResponse = http.send(request("https://api.github.com/repos/KanjiVG/kanjivg/commits/" + REVISION),
            HttpResponse.BodyHandlers.ofString());
        var shaMatch = Pattern.compile("\"sha\"\\s*:\\s*\"([0-9a-f]{40})\"").matcher(revisionResponse.body());
        if (revisionResponse.statusCode() != 200 || !shaMatch.find()) {
            throw new IllegalStateException("Cannot resolve KanjiVG revision: HTTP " + revisionResponse.statusCode());
        }
        String sha = shaMatch.group(1);
        System.out.println(sha);
        Files.createDirectories(Path.of(TEMP));
        var json = new StringBuilder("{\n");
        var hashes = new StringBuilder();
        for (int index = 0; index < GLYPHS.length(); index++) {
            char glyph = GLYPHS.charAt(index);
            String name = "%05x".formatted((int) glyph);
            var response = http.send(request("https://raw.githubusercontent.com/KanjiVG/kanjivg/" + sha + "/kanji/" + name + ".svg"),
                HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) throw new IllegalStateException("HTTP " + response.statusCode() + " for " + name);
            String svg = new String(response.body(), StandardCharsets.UTF_8);
            if (!svg.contains("Ulrich Apel") || !svg.contains("by-sa/3.0") || !svg.contains("viewBox=\"0 0 109 109\"")) {
                throw new IllegalStateException("Unexpected SVG metadata " + name);
            }
            Files.write(Path.of(TEMP + name + ".svg"), response.body());
            var path = PATH.matcher(svg);
            var strokes = new ArrayList<String>();
            while (path.find()) {
                if (!path.group(1).equals(name) || Integer.parseInt(path.group(2)) != strokes.size() + 1) {
                    throw new IllegalArgumentException("Unexpected stroke id in " + name);
                }
                strokes.add("    [" + sample(path.group(3)) + "]");
            }
            if (strokes.isEmpty()) throw new IllegalArgumentException("No strokes in " + name);
            if (index > 0) json.append(",\n");
            json.append("  \"").append(glyph).append("\": [\n").append(String.join(",\n", strokes)).append("\n  ]");
            hashes.append(name).append(".svg ");
            hashes.append(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(response.body())));
            hashes.append(" (").append(strokes.size()).append(" strokes)\n");
        }
        Files.writeString(Path.of(TEMP + "templates.json"), json.append("\n}\n"));
        Files.writeString(Path.of(TEMP + "hashes.txt"), sha + "\n" + hashes);
    }

    private static HttpRequest request(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).build();
    }

    private static String sample(String path) {
        var tokens = TOKEN.matcher(path).results().map(match -> match.group()).toList();
        var samples = new ArrayList<String>();
        char command = ' ';
        char lastCommand = ' ';
        double x = 0, y = 0, previousX = 0, previousY = 0;
        int index = 0;
        while (index < tokens.size()) {
            String token = tokens.get(index);
            if (Character.isLetter(token.charAt(0))) {
                command = token.charAt(0);
                index++;
            }
            boolean relative = Character.isLowerCase(command);
            double dx = relative ? x : 0;
            double dy = relative ? y : 0;
            switch (Character.toUpperCase(command)) {
                case 'M', 'L' -> {
                    x = dx + Double.parseDouble(tokens.get(index++));
                    y = dy + Double.parseDouble(tokens.get(index++));
                    samples.add(point(x, y));
                }
                case 'C', 'S' -> {
                    boolean smooth = Character.toUpperCase(command) == 'S';
                    boolean previousCubic = lastCommand == 'S' || lastCommand == 'C';
                    double x1 = smooth ? (previousCubic ? 2*x-previousX : x) : dx + Double.parseDouble(tokens.get(index++));
                    double y1 = smooth ? (previousCubic ? 2*y-previousY : y) : dy + Double.parseDouble(tokens.get(index++));
                    double x2 = dx + Double.parseDouble(tokens.get(index++));
                    double y2 = dy + Double.parseDouble(tokens.get(index++));
                    double endX = dx + Double.parseDouble(tokens.get(index++));
                    double endY = dy + Double.parseDouble(tokens.get(index++));
                    for (int i = 1; i <= 16; i++) {
                        double t = i/16.0;
                        double u = 1-t;
                        samples.add(point(u*u*u*x + 3*u*u*t*x1 + 3*u*t*t*x2 + t*t*t*endX,
                            u*u*u*y + 3*u*u*t*y1 + 3*u*t*t*y2 + t*t*t*endY));
                    }
                    x = endX;
                    y = endY;
                    previousX = x2;
                    previousY = y2;
                }
                default -> throw new IllegalArgumentException("Unrecognized SVG path command " + command);
            }
            lastCommand = Character.toUpperCase(command);
            if (command == 'M') command = 'L';
            if (command == 'm') command = 'l';
        }
        return String.join(",", samples);
    }

    private static String point(double x, double y) {
        if (x < 0 || y < 0 || x > 109 || y > 109) {
            throw new IllegalArgumentException("Point outside source viewbox");
        }
        return "[%.3f,%.3f]".formatted(x, y);
    }
}
