# KanjiVG stroke templates

Copyright (C) 2009/2010/2011 Ulrich Apel. The source diagrams and the derived `templates.json` geometry are distributed under Creative Commons Attribution-ShareAlike 3.0. Refer to [KanjiVG](https://kanjivg.tagaini.net/) and the [retained license](KanjiVG-LICENSE.txt).

The original 15 SVG files in `source/` come from the [KanjiVG revision d95a97627fd9fe5b2c8d06ca81e38149609c0c1e](https://github.com/KanjiVG/kanjivg/tree/d95a97627fd9fe5b2c8d06ca81e38149609c0c1e/kanji), retrieved 14 November 2026. Each filename is the Unicode code point padded to five lowercase hex digits.

Geometry format 1 maps each character to an ordered list of strokes. Each stroke is a list of `[x,y]` points in the original 109×109 viewbox with Y increasing downwards. The conversion used only path data from consecutively numbered stroke paths, including relative and smooth cubic commands. Each cubic was sampled at 16 intervals and points rounded to 0.001 viewbox units. The game loads only the derived JSON. Source SVGs were retained for audit. No remote DTD or other SVG resource runs.

The corpus contains one canonical diagram per character. A Japanese-literate human reviewer and beginner handwriting corpus are pending. These templates are for experimental gameplay and guided practice.

SHA-256 of `templates.json` is `59c900315caa458a8ee13700ca59c5e102bb34fe1930699a5f3bfebd23c959ed`.

| Glyph | Upstream SVG | Strokes | SHA-256 |
| --- | --- | ---: | --- |
| 水 | 06c34.svg | 4 | 17025cc265555ea3172b361fee740e58f178c7ed61fb7d1c7124175358f2d41e |
| 火 | 0706b.svg | 4 | ad5412383753877b4428557883ccc19b6c70b1128a06168d49ad20e31ed645a2 |
| 金 | 091d1.svg | 8 | cc6324e83b4a751cb04050ca604571141e6ec3612ad83bbe2f05ef01864876e4 |
| 木 | 06728.svg | 4 | 8ad9a1fa18fe9d0910db9686e7b05f11784650b46aec4795ac67696cc5197acd |
| 土 | 0571f.svg | 3 | 6f0d45e15f06fe5e9a0748d852d3c5a3c88b26852e1afc04c721424cd6547ea7 |
| 犬 | 072ac.svg | 4 | 6a4dd08801e607b00e49905db253159c25c064445668d4463f49fe937dd616a7 |
| 猫 | 0732b.svg | 11 | 237fc454b47486282ff0caf947a87be9a3311ed34543c4fa57456ff64f8e7b4a |
| 鳥 | 09ce5.svg | 11 | 7d9ec15c1367f8d385df4fd8395df4372d859c339242593fd9aa68a79d5ae761 |
| 魚 | 09b5a.svg | 11 | 0a7a54a98df30902bbda2e46da145c9a55aa23faf9b3227781f10fec57bbf77a |
| 馬 | 099ac.svg | 10 | dd6eb98adfde4bbb3e294ad437a388524cf4d10a3c327d920f18c650a900817f |
| 雨 | 096e8.svg | 8 | 040a7401a82f5408f63dbd0cdab266b8229940768a22f628d35dbe736f0235a0 |
| 風 | 098a8.svg | 9 | 02478eab2942f4c015140219004583c2d742e07a5ca3b3df9e70e9136af01e9a |
| 雪 | 096ea.svg | 11 | 0fe8dbb60514fe2abc487572a66c6aa8c31159e7fb61ee0e039382d4f8b95af5 |
| 雲 | 096f2.svg | 12 | 61088e1441f533707e1255515b0b94a1247b1eaf7cccf327f6020d3b467043d5 |
| 空 | 07a7a.svg | 8 | c2613d7b8db35b9ad053b9708dd6888af9abe2bbe3ce09dd5ceca322fd6d8b21 |
