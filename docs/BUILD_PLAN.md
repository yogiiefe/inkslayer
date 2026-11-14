# INKSLAYER! — rencana build

Disusun berdasarkan permintaan terbaru pada 14 November 2026. Dokumen ini merupakan rencana implementasi. Angka balancing, nama lokasi, daftar kosakata, pembagian ujian, jumlah slot, dan durasi di bawah merupakan usulan untuk playtest.

## Arah produk

Game desktop RPG 2D side-scrolling dengan pertarungan bergiliran dan pembelajaran singkat bahasa Jepang. Pemain menulis Kanji untuk menyerang serta menahan serangan elemen monster. Kampanye terdiri dari empat stage, masing-masing sepuluh encounter. Mr. Kotoba mengakhiri stage keempat dengan ujian materi yang telah diajarkan.

Kebutuhan yang ditetapkan pengguna mencakup Main Menu, Settings, daftar save, opening cinematic, empat tema stage, lima elemen sesuai gambar, serangan lintas elemen pada monster, defensive counter, dan ending cinematic. Tampilan arena mengikuti sketsa sebelumnya, dengan player di kiri, monster di kanan, garis lantai, serta area gambar di tengah bawah.

Usulan penghitungan konten adalah 30 monster pada stage 1–3, sembilan penguji pada stage 4, dan satu Mr. Kotoba. Totalnya 40 encounter. Variasi karakter dapat memakai kerangka animasi yang sama.

## Bukti prototipe yang tersedia

| Bagian | Kondisi kode saat perencanaan |
| --- | --- |
| Stack | Java 21, libGDX 1.14.2, LWJGL3, Gradle wrapper |
| Layar | Window 1280×720, minimum 960×540, FitViewport dengan letterboxing |
| Arena | Player kiri, monster besar kanan, latar gelap, garis lantai |
| Area gambar | 584×250 koordinat UI, mulai x328 dan y0 |
| Guide | Ukuran 176×176 di dalam area gambar |
| Gameplay | Satu rute horizontal dan satu monster Fire |
| Recognition | Satu karakter 水, empat stroke, evaluasi lokal deterministik |
| Combat | Serangan Water lalu balasan monster otomatis |
| Menu | Play, Settings, Quit serta pause dan journal |
| Simulasi | 60 tick per detik, gerak 155 world units/detik |

Pekerjaan baru mencakup Metal, recognition banyak karakter, defensive counter, progression 40 encounter, kurikulum kana, penyimpanan, audio dan cinematic. Kode menjadi acuan kondisi implementasi karena beberapa dokumen lama masih memuat rancangan platformer.

## Alur pemain

```text
Main Menu
  New → pilih slot → opening cinematic → Stage 1
  Save → daftar slot → Load → checkpoint terakhir
  Settings → Master / SFX / BGM → kembali
  Exit → tutup game

Stage
  jalan → micro-lesson → encounter → hasil → autosave → lanjut

Stage 1 → Stage 2 → Stage 3 → Stage 4 → Mr. Kotoba
  → ending cinematic → simpan status tamat → Main Menu
```

New memulai kampanye setelah slot dipilih. Slot terisi membutuhkan konfirmasi sebelum ditimpa. Save membuka daftar permainan tersimpan dengan tombol Load. Pause menyediakan Save & Return to Menu pada checkpoint aman. Pemain dapat membaca ulang materi yang sudah dibuka melalui journal.

## Tampilan dan input

Resolusi desain tetap 1280×720. Arena memakai kira-kira 470 piksel bagian atas, dengan 250 piksel bagian bawah untuk area gambar. Koordinat UI memakai origin kiri bawah. Kamera eksplorasi bergeser horizontal dan kamera battle menempatkan aktor pada posisi tetap.

| Elemen layar | Rencana |
| --- | --- |
| Player | Sisi kiri, kaki sejajar lantai, tinggi sekitar 175–190 piksel |
| Monster | Sisi kanan, siluet lebih besar, tinggi sekitar 340–380 piksel |
| Area gambar | x328, y0, lebar 584, tinggi 250 |
| Background | Gelap dan sederhana, warna aksen mengikuti tema stage |
| Informasi battle | Satu baris kontekstual dalam area gambar |
| Informasi elemen | Simbol Kanji, nama elemen, dan isyarat bentuk pada aktor/telegraph |
| Bantuan | Guide dan petunjuk input muncul saat pengenalan materi atau ketika diminta |

Saat menyerang, prompt dapat berbunyi “Giliranmu — gambar elemen”. Saat bertahan, prompt menyebut elemen serangan masuk. Feedback menggantikan baris yang sama. HP dan rincian elemen dapat dibaca lewat informasi battle di pause/journal agar arena mengikuti komposisi sketsa.

Mouse merekam stroke. Enter mengirim gambar, Ctrl+Z membatalkan stroke terakhir, R membersihkan, Escape membuka pause/menutup panel. A/D atau panah menggerakkan player saat eksplorasi. Saat bertahan, `G` memilih Guard dan prompt menampilkan keterangan singkat. Petunjuk kontrol diajarkan di encounter pertama dan tersedia di bantuan pause. Klik UI harus memakai transform viewport yang sama dengan rendering.

## Lima elemen dan counter

Panah pada gambar ditafsirkan sebagai “mengalahkan”. Relasinya adalah Water → Fire → Metal → Wood → Earth → Water.

| Elemen | Kanji | Mengalahkan | Dikalahkan oleh |
| --- | --- | --- | --- |
| Water | 水 | Fire | Earth |
| Fire | 火 | Metal | Water |
| Metal | 金 | Wood | Fire |
| Wood | 木 | Earth | Metal |
| Earth | 土 | Water | Wood |

Label mù, huǒ, tǔ, jīn dan shuǐ pada referensi menggunakan pelafalan Mandarin. Materi game memakai pembacaan bahasa Jepang dalam konteks kata. Untuk pengenalan awal, usulan kata adalah 水・みず, 火・ひ, 木・き, 土・つち dan 金・きん. Kata 金 pada pelajaran awal dijelaskan sebagai emas, sementara kategori spell-nya bernama Metal. Kanji ini juga memiliki makna logam dan uang sesuai konteks. Referensi pengenalan bacaan tersedia dari [Japan Foundation](https://www.jpf.go.jp/j/project/japanese/teach/tsushin/bn/dw_pdfs/tushin47_p18-19.pdf), dan rincian 金 dari [Kanjipedia](https://www.kanjipedia.jp/kanji/0001604700).

Hubungan counter menjadi aturan fiksi game. Tema hewan dan cuaca memberikan materi belajar serta variasi visual. Lima kategori elemen tetap menjadi dasar combat.

### Serangan pemain

Monster mempunyai satu `mainElement` yang tetap sepanjang encounter. Damage dihitung dari elemen spell yang dikenali terhadap `mainElement`.

Usulan awal memakai base damage 10 dan multiplier berikut.

| Kondisi | Multiplier |
| --- | --- |
| Spell mengalahkan elemen utama monster | 2× |
| Elemen sama atau elemen monster mengalahkan spell | 0,5× |
| Pasangan lainnya | 1× |

Contoh monster berelemen Fire menerima 20 damage dari Water. Serangan Water tetap mengecek elemen utama Fire walaupun monster bersiap memakai Wood.

### Serangan monster dan defensive counter

Setiap monster memiliki daftar attack yang masing-masing menyimpan `element` dan `baseDamage`. Pola serangan berurutan membuat pemilihan serangan mudah dipelajari. Elemen serangan dipilih dan dikunci sebelum telegraph muncul.

```text
Player attack input → recognition → damage
  → monster kalah? hasil kemenangan
  → monster memilih serangan → telegraph
  → player menggambar counter atau memilih Guard
  → resolve satu kali → Player attack input
```

Counter mengecek `incomingAttack.element`. Monster Fire yang memakai serangan Wood harus ditahan dengan Metal. Teks telegraph menampilkan Wood agar pemain mendapat informasi yang cukup.

Usulan awal pertahanan adalah counter benar mencegah seluruh damage, Guard menerima separuh damage dengan pembulatan ke atas, dan elemen yang dikenali tetapi salah menerima damage penuh. Counter berhasil mengakhiri serangan tersebut lalu memberi giliran menyerang kembali. Damage balasan tambahan dapat dievaluasi setelah playtest.

Gambar yang tidak terbaca atau ambigu mempertahankan giliran dan memberi satu petunjuk koreksi. Karakter yang berhasil dikenali sebagai elemen lain diperlakukan sebagai pilihan elemen pemain. Enter ganda harus menghasilkan satu aksi. Mode belajar menyediakan waktu gambar tanpa batas dan bantuan yang dapat dibuka kembali.

Usulan balancing awal adalah player 30 HP, monster biasa 30–50 HP, elite 60–80 HP, dan serangan monster 6–10 damage. HP player pulih pada checkpoint setelah kemenangan. Target monster biasa selesai dalam sekitar 2–4 serangan efektif. Semua angka memerlukan simulasi dan playtest sebelum dijadikan balancing final.

## Pembelajaran mikro

Usulan lingkup kurikulum pertama adalah 15 Kanji, bacaan hiragana yang menyertainya, dan sekitar delapan kata katakana yang relevan dengan barang atau hewan. Silabus kana berfokus pada simbol yang dipakai kata tersebut. Penguasaan lengkap seluruh kana menjadi sasaran yang dapat diperluas setelah evaluasi durasi belajar.

Satu micro-lesson mengenalkan satu kata atau beberapa kana terkait selama kira-kira 20–45 detik. Urutannya adalah makna/gambar → bunyi dan bacaan → contoh bentuk → latihan dibantu → recall → review beberapa encounter kemudian. Durasi menjadi target desain dan pemain tetap dapat membaca dengan kecepatannya sendiri.

| Sistem tulisan | Aktivitas inti | Bukti yang dicatat |
| --- | --- | --- |
| Hiragana | Mendengar atau melihat kata, memilih/menyusun bacaan yang telah diajarkan | Jawaban mandiri, bantuan, kesalahan |
| Katakana | Mencocokkan kata benda dengan gambar/bunyi, kemudian membaca ulang | Pengenalan simbol dan kata |
| Kanji | Menghubungkan arti, bacaan, dan bentuk lalu menggambar | Bentuk, urutan/arah coaching, penggunaan guide |

Untuk build awal, drawing recognition digunakan pada Kanji. Saat soal kana muncul, area bawah menampilkan prompt dan kartu jawaban. Pemain memilih dengan klik atau tombol panah lalu mengonfirmasi dengan Enter. Tanda kecil, dakuten dan vokal panjang diajarkan saat pertama kali muncul. Romaji tersedia sebagai penyangga awal dan bisa disembunyikan. Penggunaan hiragana, katakana, dan Kanji pada kata perlu mengikuti konteks bahasa Jepang. [Japan Foundation, Irodori](https://www.irodori.jpf.go.jp/assets/data/starter/pdf/X_L03.pdf)

Kanji bertema pet dan cuaca dipakai dalam latihan dan segel ujian. Lima Kanji elemen menjalankan spell combat. Jawaban latihan yang keliru memberi feedback dan percobaan ulang. Salah memilih elemen combat mengikuti aturan damage. Pemain dapat membuka bantuan setelah keliru tanpa harus mengulang stage.

Data belajar membedakan sukses dengan tracing, sukses dengan petunjuk, dan recall mandiri. Review berikutnya dipilih berdasarkan materi yang paling lama ditinjau dan materi yang sering keliru. Save menyimpan perbedaan tersebut.

Daftar pet/cuaca berikut adalah kandidat materi, dengan bacaan kontekstual, stroke, varian dan lisensi yang harus diperiksa pada tahap authoring. Setiap entri memiliki ID, karakter, bacaan kana, arti, contoh kata bila diperlukan, sumber bacaan, sumber stroke, revisi sumber, lisensi, dan status review penutur/pengajar Jepang. Referensi kurikulum dapat diperiksa melalui [Irodori](https://www.irodori.jpf.go.jp/en/resources.html). Sumber jalur stroke mengikuti kebijakan sumber proyek di [.agents/skills/kanji-handwriting-recognition/references/sources.md](../.agents/skills/kanji-handwriting-recognition/references/sources.md).

## Empat stage dan 40 encounter

Nama stage dan karakter kerja di bawah merupakan usulan kreatif. Setiap baris merupakan satu monster/encounter. Attack sekunder mulai digunakan setelah semua counter terkait diajarkan. Seluruh pola bersifat deterministik pada build awal.

### Stage 1 — Elements

Usulan tema adalah gerbang inspeksi dengan aksen lima elemen. Tujuan utamanya mempelajari lima spell, hiragana dari bacaan pendek, dan perbedaan elemen utama dengan elemen serangan. Kata katakana awal dapat memakai barang yang ditemui player, misalnya ペン dan インク, setelah review bahasa.

| Encounter | Elemen utama monster | Pola serangan | Pelajaran / tujuan |
| --- | --- | --- | --- |
| 1. Ember | Fire | Fire | Pelajari 水, serang dan tahan Fire dengan Water |
| 2. Iron | Metal | Metal | Pelajari 火, counter Metal |
| 3. Thorn | Wood | Wood | Pelajari 金, counter Wood |
| 4. Clay | Earth | Earth | Pelajari 木, counter Earth |
| 5. Mist | Water | Water | Pelajari 土, counter Water |
| 6. Ember II | Fire | Fire → Wood | Bedakan target serangan dengan incoming attack |
| 7. Iron II | Metal | Metal → Water | Review 火 dan 土 |
| 8. Thorn II | Wood | Wood → Fire | Review 金 dan 水 |
| 9. Clay II | Earth | Earth → Metal | Review 木 dan 火 |
| 10. Gate Inspector | Water | Water → Earth → Fire | Review gabungan lima elemen |

Sebelum encounter 1, tutorial aman mengenalkan empat stroke 水. Kelima spell dibuka bertahap sebelum dipakai sebagai counter wajib. Setiap karakter baru menerima contoh dan latihan dibantu. Review hiragana berselang di sela encounter.

### Stage 2 — Pets

Usulan tema adalah kawasan hewan dengan monster berwujud hewan fantasi. Kandidat Kanji adalah 犬, 猫, 鳥, 魚 dan 馬. Katakana dikenalkan melalui kata terkait hewan/peralatan, misalnya ペット, ケージ dan ハムスター, setelah review bahasa. Pembelajaran berlangsung lewat observasi hewan dan dialog singkat.

| Encounter | Elemen utama | Pola serangan | Pelajaran / tujuan |
| --- | --- | --- | --- |
| 1. Ember Hound | Fire | Fire → Earth | Kenalkan 犬 |
| 2. Moss Cat | Wood | Wood → Water | Kenalkan 猫 |
| 3. Iron Bird | Metal | Metal → Fire | Kenalkan 鳥 |
| 4. Mist Fish | Water | Water → Wood | Kenalkan 魚 |
| 5. Clay Horse | Earth | Earth → Metal | Kenalkan 馬 |
| 6. Hound Sentinel | Fire | Wood → Fire | Review 犬 dan bacaan hiragana |
| 7. Cat Sentinel | Wood | Fire → Wood | Review 猫 dan katakana |
| 8. Bird Sentinel | Metal | Water → Metal | Review 鳥 dan incoming attack |
| 9. Fish Sentinel | Water | Earth → Water | Review 魚 serta materi stage 1 |
| 10. Stable Inspector | Earth | Metal → Earth → Wood | Review 馬 dan lima kata stage 2 |

### Stage 3 — Weather

Usulan tema adalah rute cuaca dengan perubahan warna dan animasi ringan pada monster. Kandidat Kanji adalah 雨, 風, 雪, 雲 dan 空. Contoh kata katakana terkait cuaca/pakaian dapat mencakup コート, ブーツ dan タオル setelah review bahasa. Atmosfer mendukung keterbacaan arena dan tulisan.

| Encounter | Elemen utama | Pola serangan | Pelajaran / tujuan |
| --- | --- | --- | --- |
| 1. Rain Wisp | Water | Water → Metal | Kenalkan 雨 |
| 2. Gale Wisp | Wood | Wood → Earth | Kenalkan 風 |
| 3. Snow Wisp | Metal | Metal → Water | Kenalkan 雪 |
| 4. Cloud Wisp | Earth | Earth → Fire | Kenalkan 雲 |
| 5. Dusk Wisp | Fire | Fire → Wood | Kenalkan 空 |
| 6. Rain Sentinel | Water | Fire → Water | Review 雨 dan Water/Fire |
| 7. Gale Sentinel | Wood | Metal → Wood | Review 風 dan bacaan kana |
| 8. Snow Sentinel | Metal | Earth → Metal | Review 雪 serta katakana |
| 9. Cloud Sentinel | Earth | Wood → Water → Earth | Review 雲 dan materi lama |
| 10. Weather Inspector | Fire | Water → Fire → Metal | Review 空 dan lima kata stage 3 |

Penetapan elemen monster cuaca merupakan pilihan combat. Contohnya Snow Wisp mempunyai elemen utama Metal untuk variasi lawan, dan label elemen menampilkan pilihan tersebut secara eksplisit.

### Stage 4 — Mr. Kotoba's Examination

Usulan tema adalah ruang pemeriksaan akhir. Materi baru berakhir di stage 3. Stage 4 mengulang materi yang sudah masuk riwayat belajar pemain.

| Encounter | Elemen utama | Pola serangan | Fokus ujian |
| --- | --- | --- | --- |
| 1. Examiner I | Fire | Fire → Water | Arti Water dan Fire |
| 2. Examiner II | Metal | Metal → Earth | Metal dan Wood |
| 3. Examiner III | Water | Water → Wood | Earth dan review lima counter |
| 4. Examiner IV | Wood | Wood → Metal | Kata hewan dan bacaan hiragana |
| 5. Examiner V | Earth | Earth → Fire | Kata hewan dan katakana |
| 6. Examiner VI | Fire | Wood → Fire | Recall Kanji pet |
| 7. Examiner VII | Water | Metal → Water | Kata cuaca |
| 8. Examiner VIII | Metal | Fire → Earth | Bacaan cuaca dan kata katakana |
| 9. Examiner IX | Wood | Water → Metal → Wood | Review campuran dengan bantuan opsional |
| 10. Mr. Kotoba | Metal | Fire → Water → Wood → Earth → Metal | Seluruh 15 Kanji dan review kana |

Mr. Kotoba mempunyai elemen utama Metal yang tetap. Usulan boss memakai tiga ronde dengan lima soal Kanji per ronde. Tiap ronde menggabungkan soal arti, bacaan dan tulisan, lalu menguji pertahanan dan serangan.

| Ronde | Kanji yang diuji | Serangan pembuka Kotoba |
| --- | --- | --- |
| Elemen | 水, 火, 金, 木, 土 | Fire, lalu Water |
| Hewan | 犬, 猫, 鳥, 魚, 馬 | Wood, lalu Earth |
| Cuaca | 雨, 風, 雪, 雲, 空 | Metal |

Setelah soal satu ronde dijawab, pemain bertahan dari serangan pembukanya dan mendapat giliran menyerang. Setiap ronde punya 20 HP untuk uji balancing, diisi saat ronde dimulai. HP ronde yang habis membuka ronde berikutnya. Kemenangan dan ending baru dipicu setelah ronde ketiga selesai dan semua 15 ID Kanji tercatat. Jika pemain perlu beberapa serangan untuk menghabiskan HP ronde, Kotoba mengulang serangan terakhir ronde itu antara giliran pemain. Kesalahan pelajaran diulang dengan bantuan dan kesalahan defensive counter mengikuti aturan HP. Retry kembali ke checkpoint sebelum boss.

## Opening dan ending cinematic

Format awal berupa 5–7 panel pixel-art dengan caption, musik, SFX, perpindahan kamera sederhana, tombol Next dan Skip. Target durasi masing-masing 45–90 detik. Script disimpan sebagai daftar panel dan caption.

Opening mengikuti lore yang telah ada. Rezim memaksakan bahasa Jepang, Mr. Kotoba mengadakan inspeksi, protagonis terlewat dari daftar, lalu bergerak menuju wilayah inspeksi pertama. Detail dialog serta urutan shot ditulis sebagai usulan storyboard sebelum produksi.

Usulan ending memperlihatkan player menyelesaikan ujian, menghadapi reaksi Mr. Kotoba, dan menutup konflik kampanye dengan perubahan keadaan yang jelas. Pilihan nasib Mr. Kotoba dan bentuk perubahan dunia ditetapkan saat review script. Penyimpanan status tamat dan pilihan replay tetap berfungsi ketika cinematic dilewati.

## Save dan settings

Usulan awal menyediakan tiga slot kampanye. Setiap slot memiliki satu autosave aktif dan satu backup yang dibuat otomatis. Menu Save menampilkan semua slot, stage, encounter berikutnya, waktu bermain, waktu simpan dan persentase kampanye.

Simpan setelah opening, kemenangan encounter, perpindahan stage dan ending. Saat Kotoba kalah, simpan checkpoint yang siap memutar ending. Load dari checkpoint tersebut memulai ending, dan menonton sampai selesai atau memilih Skip menyimpan status tamat. Save & Return dari battle kembali melanjutkan checkpoint sebelum battle tersebut, dengan penjelasan pada tombol konfirmasi. Penulisan file memakai temporary file lalu penggantian atomik jika filesystem mendukungnya dan backup pemulihan jika gagal.

Isi save mencakup versi format dan konten, slot ID, stage ID, encounter ID, daftar encounter selesai, spell terbuka, progres belajar, status cinematic, waktu bermain dan status tamat. Save lama memakai migrasi bila format berubah. Save rusak menawarkan backup atau slot lain. Progres slot baru dimulai dengan tutorial sendiri.

Settings menyimpan Master, SFX dan BGM pada rentang 0–100%, dengan preview saat slider digerakkan. Volume efektif SFX adalah Master × SFX dan volume musik adalah Master × BGM setelah normalisasi 0–1. Audio pelafalan memakai kategori SFX dan dapat diputar ulang. Preferensi audio disimpan terpisah dari slot kampanye dan kembali aktif pada startup.

## Asset dan produksi konten

Daftar minimum produksi mencakup satu player, 40 definisi encounter, karakter/varian monster bertema, Mr. Kotoba, empat variasi arena, lima efek serangan dan counter, aset UI, opening/ending, BGM dan SFX.

Player membutuhkan idle, walk, cast, guard/counter, hit dan defeat. Monster membutuhkan idle, attack, hit dan defeat. Mr. Kotoba menambahkan pose untuk perpindahan ronde. Ukuran frame aset ditetapkan setelah mockup battle pertama, dengan skala pixel yang konsisten dan ruang untuk efek tanpa menutupi tulisan.

Audio awal mencakup satu musik menu, satu musik per stage, satu musik boss, satu musik ending, suara UI, stroke/submit, lima elemen, counter, hit dan hasil battle. Pelafalan diproduksi/diperiksa untuk kosakata yang benar-benar masuk kurikulum. Setiap aset pihak ketiga menyimpan asal, lisensi dan kredit. Aset raster dan suara yang masuk proyek berasal dari hasil authoring atau sumber berlisensi yang disepakati.

## Struktur implementasi yang diusulkan

Kode yang ada tetap menjadi titik masuk untuk pengembangan. `World` menangani perjalanan dan combat, `GameScreen` input dan pergantian mode, `ScreenView` tampilan, `Drawing` capture, dan `KanjiRecognizer` pengenalan. Batas baru ditambahkan ketika fitur bersangkutan dikerjakan.

```text
assets/
  data/
    elements.json
    monsters.json
    stages/stage01.json ... stage04.json
    learning/entries.json
    learning/lessons.json
    kanji/                 # template stroke dan attribution
    cutscenes/opening.json, ending.json
  fonts/
  sprites/player/, monsters/, kotoba/
  stages/
  ui/
  audio/bgm/, sfx/, pronunciation/
  CREDITS.md

core/src/main/java/com/yogiiefe/inkslayer/
  screens/                 # game, menu, save, cinematic
  world/                   # state combat dan progression
  recognition/             # capture dan matching
  learning/                # lesson dan riwayat review
  save/                    # DTO, baca/tulis, migrasi
  audio/                   # loading, playback, volume

lwjgl3/                    # launcher dan packaging desktop
docs/                      # desain, sumber, hasil pemeriksaan
```

Folder di atas adalah target rancangan. Lokasi save runtime memakai direktori data pengguna dengan pola `INKSLAYER/saves/slot-01.json`, terpisah dari aset dalam JAR. Lokasi absolut ditetapkan oleh launcher sesuai platform.

Setiap stage menyimpan urutan sepuluh encounter. Setiap monster menyimpan ID, elemen utama, HP, pola attack, lesson ID dan art ID. Engine combat yang sama menjalankan seluruh encounter. Sekuen cutscene cukup memakai data panel. Perjalanan memakai posisi horizontal dan trigger encounter.

Risiko pertama adalah recognizer saat ini yang mengasumsikan satu karakter dan empat stroke. Pengembangan harus menambahkan template per karakter, jumlah stroke yang bervariasi, klasifikasi kandidat yang sudah dibuka, serta penolakan hasil ambigu. Matcher harus mengenali apa yang digambar sebelum aturan combat menentukan hasil. Benchmark khusus diperlukan untuk 金, 猫, 雲 dan bentuk yang mirip. Kandidat yang banyak stroke memerlukan pengukuran biaya assignment dan batas input yang eksplisit.

## Urutan pembangunan dan kriteria selesai

| Tahap | Hasil yang dapat dimainkan/diperiksa | Kriteria selesai |
| --- | --- | --- |
| 1. Kontrak konten | Lima elemen, daftar kata, sketsa menu, storyboard | Relasi gambar, daftar 40 encounter, prasyarat pelajaran dan metadata sumber konsisten |
| 2. Combat + recognition | Satu monster dengan dua jenis serangan, lima Kanji | Offensive weakness dan defensive counter mengikuti elemen yang benar, ambiguity menahan aksi, submit sekali menghasilkan satu aksi |
| 3. Alur satu sesi | New → opening → 2 encounter → save → quit → load | Slot kembali ke checkpoint dan progres belajar yang sama, skip cinematic aman, slider audio bekerja |
| 4. Stage 1 lengkap | Sepuluh encounter dan lima spell | Pemula dapat menyelesaikan tutorial, semua counter tersedia sebelum wajib, resize dan input dapat dipakai |
| 5. Stage 2–3 | Dua puluh encounter berikutnya, pet/cuaca/kana | Materi sumber direview, guide/recall tercatat, review berselang berjalan, aset terbaca |
| 6. Stage 4 + ending | Sembilan penguji, Kotoba, ending | Semua Kanji wajib tercakup, attack lima elemen terbaca, tamat tersimpan, retry berfungsi |
| 7. Release candidate | Paket desktop kampanye lengkap | JAR berjalan di luar folder proyek, save recovery diuji, lisensi lengkap, full playthrough dan audio/visual QA selesai |

Tahap 2 menjadi gate sebelum memperbanyak konten. Tahap 3 membuktikan seluruh jalur permainan dalam skala kecil sebelum produksi 40 encounter. Estimasi kalender baru dihitung setelah dua tahap tersebut memperlihatkan biaya recognition dan produksi aset.

## Verifikasi dan evaluasi

Tes logika memeriksa semua 25 pasangan elemen, offensive target berbeda dari incoming attack, counter benar/salah/Guard, repeated submit, victory tanpa balasan ekstra, defeat/retry, progression tepat 40 encounter dan cakupan soal boss. Save diuji untuk round-trip, slot isolation, overwrite, backup, kegagalan tulis dan migrasi.

Desktop QA menjalankan alur normal dari menu, menulis dengan mouse, pause saat input, resize 1280×720/960×540/ultrawide, focus loss, skip kedua cinematic, volume 0/100%, load dan tamat. Font harus menampilkan seluruh glyph konten. Recognition diperiksa dengan contoh tulisan pemula yang terpisah dari data tuning, termasuk karakter mirip dan scribble. Hasil tes sintetis dan hasil belajar manusia dicatat masing-masing sesuai metodenya.

Pilot awal dapat melibatkan 5–8 pemula untuk mengamati apakah mereka memahami counter, berapa kali tulisan sah ditolak, kebutuhan bantuan, kelelahan menggambar dan recall setelah jeda. Jumlah peserta ini merupakan usulan studi awal. Kriteria dan target angka ditentukan sebelum pilot, lalu tuning mengikuti temuan.

## Batas build pertama dan keputusan terbuka

Prioritas rilis pertama adalah kampanye linear, satu player, lima elemen, 40 encounter, belajar mikro, save lokal, audio, opening dan ending. Inventory, equipment, party, pet collection, crafting, multiplayer, skill tree dan procedural generation dapat dibahas setelah kampanye inti terbukti.

Keputusan yang perlu review pada milestone konten mencakup jumlah kata kana final, cara pengucapan dan pelokalan UI, identitas visual monster, script ending, jumlah slot, kebutuhan runtime Java yang dibundel, serta target perangkat. Rencana awal memakai tiga slot, 15 Kanji, sekitar delapan kata katakana, dan konten hiragana yang mendukung bacaan kata tersebut.

Dokumen ini dibuat dengan inspeksi source, data dan referensi pembelajaran. Pemeriksaan pada sesi perencanaan berupa validasi konsistensi dan tautan dokumen. Build serta hasil gameplay baru akan menjadi bagian milestone implementasi.
