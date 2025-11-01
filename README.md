# ISTQB CTFL Exam Simulator - Android App

Android uygulaması, ISTQB CTFL sınavlarını simüle eden offline çalışan bir sınav simülatörüdür.

## Özellikler

- **Gerçek Sınav Modu**: 40 soru, 60 dakika, %65 baraj ile gerçek sınav koşullarını taklit eder
- **Pratik Modu**: Sınırsız süre veya özel süre ile pratik yapma imkanı
- **Soru Karıştırma**: Seed destekli karıştırma ile sorular ve şıklar karıştırılabilir
- **İstatistikler**: Toplam deneme, ortalama başarı yüzdesi ve son denemeler grafiği
- **Detaylı İnceleme**: Sınav sonrası soru bazlı detaylı inceleme
- **Dosya İçe Aktarma**: SAF (Storage Access Framework) ile yeni soru setleri içe aktarılabilir

## Teknoloji Stack

- **Dil**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Mimari**: MVVM + Repository Pattern
- **Veritabanı**: Room
- **JSON Parsing**: kotlinx-serialization
- **Görsel Yükleme**: Coil

## Kurulum

1. Android Studio'yu açın
2. Projeyi import edin
3. Gradle sync yapın
4. Uygulamayı çalıştırın

## JSON Formatı

Soru setleri JSON formatında olmalıdır:

```json
{
  "meta": {
    "title": "Set Başlığı",
    "version": "1.0",
    "lang": "tr",
    "source": "source_id"
  },
  "questions": [
    {
      "id": 1,
      "type": "single" | "multiple",
      "score": 1,
      "lo": "FL-1.1.1",
      "kLevel": "K1",
      "text": "Soru metni",
      "options": {
        "a": "Şık A",
        "b": "Şık B",
        "c": "Şık C",
        "d": "Şık D"
      },
      "answer": ["c"] // veya ["a", "e"] multiple için
    }
  ]
}
```

## İlk Kurulum

Uygulama ilk açılışta `assets/` klasöründeki JSON dosyalarını otomatik olarak yükler:
- `istqb_sample_a.json`
- `istqb_sample_b.json`

## Soru Seti İçe Aktarma

1. Ana sayfadan "Soru Setleri" butonuna tıklayın
2. FAB (Floating Action Button) veya üst menüden "İçe Aktar" butonuna tıklayın
3. JSON dosyanızı seçin
4. Dosya otomatik olarak parse edilip veritabanına kaydedilir

## Sınav Ayarları

- **Soru Sayısı**: Varsayılan 40
- **Süre**: Varsayılan 60 dakika
- **Baraj**: Varsayılan %65
- **Karıştırma**: Sorular ve/veya şıklar karıştırılabilir
- **Seed**: Tekrarlanabilirlik için seed değeri (otomatik oluşturulur)

## Değerlendirme

- Her soru 1 puan değerindedir
- Başarı yüzdesi: (Doğru / Toplam) * 100
- Multiple sorularda tam eşleşme gerekir (ekstra şık seçilirse yanlış sayılır)
- Baraj karşılaştırması: ≥ passPercent → GEÇTİ

## Mimari

```
app/
├── data/
│   ├── local/ (Room entities, DAOs, Database)
│   ├── model/ (Domain models)
│   ├── repository/ (Data repositories)
│   └── serializer/ (JSON serialization)
├── domain/
│   └── usecase/ (Business logic)
├── ui/
│   ├── dashboard/ (Ana sayfa)
│   ├── setup/ (Sınav ayarları)
│   ├── exam/ (Sınav ekranı)
│   ├── result/ (Sonuç ekranı)
│   ├── review/ (İnceleme ekranı)
│   └── questionsets/ (Soru setleri)
└── util/ (Utilities)
```

## Lisans

Bu proje eğitim amaçlı geliştirilmiştir.

