# INKSLAYER! — Usulan alur gameplay

Pemain berjalan menyusuri stage, menemukan monster, membaca serangannya, lalu memilih kapan menulis spell baru dan kapan menggunakan spell yang sudah dikuasai. Pertarungan biasa langsung mulai saat pemain mendekati monster. Pelajaran wajib muncul saat mengenalkan spell yang akan segera dibutuhkan dan selama ujian Kotoba.

## Alur satu stage

```text
Masuk stage → eksplorasi → bertemu monster → battle
                │                              │
                └─ Interaksi pelajaran          ├─ Menang → checkpoint → lanjut
                   atau latihan                 └─ Kalah → ulang battle
Lawan ke-10 → stage berikutnya
Kotoba kalah → ending → simpan status tamat
```

Pelajaran singkat memperkenalkan arti dan bacaan, memperlihatkan urutan stroke, lalu memberi satu kesempatan tracing. Spell terbuka setelah tracing berhasil. Pertarungan pertama menyediakan guide jika diminta. Recall mandiri muncul beberapa encounter kemudian melalui pemilihan counter, latihan jurnal, atau soal singkat dari objek di stage. Progres menyimpan latihan terbantu dan recall mandiri secara terpisah. Jika kalah, pemain mengulang pertarungan dari checkpoint dan pelajaran yang sudah diselesaikan tetap tercatat.

## Alur satu battle

1. **Baca lawan.** Tampilkan elemen utama monster di dekat namanya. Elemen ini menentukan kelemahan terhadap spell pemain.
2. **Ambil giliran.** Pemain menggambar satu Kanji elemen yang sudah dibuka. Setelah spell berhasil menyerang, pemain boleh melepas spell yang sama dengan Enter pada giliran serang berikutnya. Tombol Q membuka kanvas untuk mengganti spell. Spell tersimpan berlaku sampai encounter selesai.
3. **Baca serangan masuk.** Monster menunjukkan elemen serangan berikutnya sebelum damage terjadi. Pola serangan bisa memakai elemen utama atau elemen lain yang counternya sudah diajarkan.
4. **Pilih respons.** Gambar Kanji counter yang tepat untuk membatalkan damage dan langsung memberi damage balasan. Tekan G untuk Guard dan menerima separuh damage. Jika pemain mengirim elemen yang terbaca tetapi keliru, serangan monster mengenai pemain. Gambar yang tidak terbaca mempertahankan kesempatan mencoba.
5. **Lanjutkan atau selesai.** Jika monster masih hidup, kembali ke giliran pemain. Serangan atau balasan yang menghabiskan HP monster langsung mengakhiri battle.

Usulan awal untuk playtest adalah balasan counter sebesar 10 damage tetap. Spell biasa memakai aturan kelemahan saat ini. Counter membaca elemen serangan masuk dan balasannya selesai dalam satu aksi. Cast ulang dicatat sebagai penggunaan spell dan hanya gambar baru yang bisa menambah catatan recall tulisan.

| Tipe lawan | Keputusan pemain |
| --- | --- |
| Cepat | Pakai kelemahan untuk menang dalam sekitar dua serangan. |
| Berperisai | Baca serangan untuk melakukan counter. Counter melepas perisai, lalu spell berikutnya mengenai lawan dengan damage normal. |
| Pola campuran | Pertahankan spell ofensif yang unggul sambil menggambar counter berbeda saat elemen serangan berubah. |

**Contoh Ember II.** Ember berelemen Fire, jadi pemain menggambar 水 untuk serangan pertama. Saat Ember mengumumkan Wood, pemain menggambar 金 untuk menahan serangan dan membalas. Jika Ember masih hidup, pemain bisa menekan Enter untuk memakai 水 lagi. Dua gambar Kanji dipakai untuk dua keputusan.

## Ritme kampanye

| Bagian | Alur |
| --- | --- |
| Stage 1 · Elements | Kenalkan lima spell sebelum pertama kali dibutuhkan, dengan urutan 水, 火, 金, 木, 土. Lima lawan berikutnya mempraktikkan kelemahan, perisai, dan serangan campuran memakai materi yang sudah dikenal. |
| Stage 2 · Pets | Pelajari kosakata hewan lewat tanda kandang atau interaksi singkat. Kenalkan bacaan hiragana dan kata katakana dalam konteks tersebut. Pertarungan memakai pola lawan yang berbeda. |
| Stage 3 · Weather | Temukan kosakata lewat perubahan lingkungan. Gunakan lawan berpola campuran untuk menguji apakah pemain membaca elemen serangan sebelum memilih counter. |
| Stage 4 · Examination | Sembilan penguji memvariasikan aturan battle. Kotoba menguji lima Kanji per ronde, mencampur soal arti, bacaan, dan tulisan, lalu menutup setiap ronde dengan pertukaran serangan. |

Pelajaran eksplorasi boleh ditunda. Jurnal menyediakan materi yang terlewat sebelum ronde Kotoba terkait, dengan latihan tanpa batas waktu. Ketiga ronde memakai daftar Kanji yang sudah ada serta bantuan yang dapat dibuka saat dibutuhkan.

## Uji pertama

Terapkan alur pada lima encounter awal untuk menguji tutorial dan cast ulang. Tambahkan satu lawan berperisai dan Ember II untuk menguji variasi setelah lima spell terbuka. Ukur jumlah gambar per kemenangan, durasi battle, pilihan Guard atau counter, kegagalan pengenalan, dan apakah pemain masih bisa mengingat Kanji tanpa guide beberapa encounter kemudian. Angka damage dan durasi battle ditetapkan setelah melihat hasil uji.
