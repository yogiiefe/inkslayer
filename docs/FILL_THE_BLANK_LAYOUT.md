# INKSLAYER! — Layout battle Fill the Blank

Acuan visual adalah sketsa `newLayout.PNG`. Dokumen ini merinci rancangan battle yang sedang dibahas.

```text
┌───────────────────────────────────────────────────────┐
│ [Glossarium] [Settings]      ╭────────────────────╮   │
│                              │ I drink water.     │   │
│                              │ ［　］を飲みます。   │   │
│                              ╰───────────┬────────╯   │
│                                          ▼            │
│          PLAYER                       MONSTER         │
│          [sprite]                     [sprite]        │
├───────────────────────────────────────────────────────┤
│                                                       │
│                AREA TULIS JAWABAN                     │
│                                                       │
└───────────────────────────────────────────────────────┘
```

| Area | Isi dan perilaku |
| --- | --- |
| Kiri atas | Dua tombol sementara, **Glossarium** dan **Settings**. Tampilan dan isi panelnya menyusul. |
| Kiri arena | Player berdiri di atas garis lantai dan menghadap monster. |
| Kanan arena | Monster lebih besar dari player. Ekor balon dialog menunjuk ke monster. |
| Balon dialog | Tampilkan terjemahan Inggris lengkap dan satu kalimat Jepang dengan kotak kosong. Setelah jawaban benar, isi kotaknya sebelum efek spell dan soal berikutnya. Jika jawaban perlu diperbaiki, tampilkan petunjuk singkat di balon yang sama. |
| Bawah garis lantai | Seluruh bidang dari garis lantai sampai tepi bawah, selebar layar, dapat menerima stroke Kanji. Saat soal memakai kartu kana, tampilkan kartu di area yang sama. |

Pada viewport 1280×720, letakkan lantai sekitar 460 piksel dari atas agar tersisa sekitar 260 piksel untuk menjawab. Ukuran tersebut mengikuti proporsi sketsa dan dapat disesuaikan setelah uji keterbacaan. Tema tetap pixel art gelap, dengan teks dan batas kosong yang kontras.

Saat eksplorasi, balon soal dan kanvas disembunyikan. Saat battle mulai, monster menampilkan soal pertamanya. Pemain menggambar di mana saja dalam bidang bawah dan mengirim jawaban lewat Enter. Ctrl+Z menghapus stroke terakhir, dan R mengosongkan kanvas.
