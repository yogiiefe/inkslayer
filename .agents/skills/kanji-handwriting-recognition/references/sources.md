# Recognition and Japanese data sources

Researched 2026-11-14; no dataset or upstream implementation has been imported by this setup.

- [$N author's site](https://depts.washington.edu/acelab/proj/dollar/ndollar.html): multistroke gesture recognizer, source/pseudocode and New BSD licensing. Standard $N generalizes stroke order and direction; do not call it a stroke-order validator. Full permutations scale poorly with Kanji stroke counts.
- [$P](https://depts.washington.edu/acelab/proj/dollar/pdollar.html) and [$Q](https://depts.washington.edu/acelab/proj/dollar/qdollar.html): candidate baselines for shape matching, not stroke pedagogy. Verify their actual source/license before porting or vendoring; neither provides Kanji literacy validation by itself.
- [KanjiVG repository](https://github.com/KanjiVG/kanjivg): inspect character SVG stroke paths and ordering, revisions, variants and attribution before deriving templates. Its documented CC BY-SA terms require a separate attribution/share-alike review for derived data; do not assume the game's code shares the dataset license. Confirm exact terms from the selected revision before import.
- [KANJIDIC project documentation](https://www.edrdg.org/dokuwiki/doku.php?id=kanjidic:kanjidic): intended source for code points, readings, meanings and stroke-count cross-checks. The old wiki URL redirects to a migration notice and the current endpoint was not retrievable in this environment. Linguistic entries and current reuse terms are therefore not verified here. Do not ship a vocabulary table based on this setup alone.

For each actual teaching entry, cross-check the displayed character, kana reading, contextual gloss, count, and demonstrated path order with named references; record who reviewed it. On/kun readings are context-dependent, not an interchangeable list of pronunciations. A font glyph is not a stroke-order source. Label unresolved variants instead of silently resolving them from a language model's memory.
