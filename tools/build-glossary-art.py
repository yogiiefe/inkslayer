"""Rebuild the 32px glossary writing frames from the bundled KanjiVG stroke data (Pillow)."""
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw

ASSETS = Path(__file__).resolve().parents[1] / "assets"
DATA = ASSETS / "data" / "kanji"
SIZE = 32
COLUMNS = 48
PHASES = 4


def stroke_frames(canvas, points):
    points = [(x * (SIZE - 1) / 109, y * (SIZE - 1) / 109) for x, y in points]
    distances = [math.dist(a, b) for a, b in zip(points, points[1:])]
    total = sum(distances)
    for phase in range(1, PHASES + 1):
        budget = total * phase / PHASES
        path = [points[0]]
        for a, b, length in zip(points, points[1:], distances):
            if length > budget:
                t = budget / length
                path.append(tuple(x + (y - x) * t for x, y in zip(a, b)))
                break
            path.append(b)
            budget -= length
        frame = canvas.copy()
        ImageDraw.Draw(frame).line([(round(x), round(y)) for x, y in path], fill=255, width=2)
        yield frame


def main():
    glyphs = {}
    for name in ("fill-templates.json", "glossary-kana.json"):
        glyphs.update(json.loads((DATA / name).read_text(encoding="utf-8")))
    count = sum(len(strokes) * PHASES for strokes in glyphs.values())
    atlas = Image.new("L", (SIZE * COLUMNS, SIZE * math.ceil(count / COLUMNS)))
    index = {}
    cursor = 0
    for glyph, strokes in glyphs.items():
        index[glyph] = [cursor, len(strokes) * PHASES]
        canvas = Image.new("L", (SIZE, SIZE))
        for stroke in strokes:
            for frame in stroke_frames(canvas, stroke):
                atlas.paste(frame, (cursor % COLUMNS * SIZE, cursor // COLUMNS * SIZE))
                cursor += 1
            canvas = frame
    target = ASSETS / "glossary"
    target.mkdir(exist_ok=True)
    sheet = Image.new("LA", atlas.size, (255, 0))
    sheet.putalpha(atlas)
    sheet.save(target / "writing.png", optimize=True)
    (target / "writing.json").write_text(json.dumps(index, ensure_ascii=False, indent=None), encoding="utf-8")


if __name__ == "__main__":
    main()
