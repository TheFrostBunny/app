# BussWidget 🚌

Android-widget som viser sanntidsavganger fra norske bussholdeplasser via Entur sitt åpne API.

## Funksjoner
- **3 widgetstørrelser**: Liten (2×1), Medium (4×1), Stor (4×2)
- **Søk etter holdeplass** direkte i appen (alle norske holdeplasser)
- **Sanntidsdata** fra Entur – dekker Ruter, AtB, Skyss og alle andre
- **Oppdateres automatisk** hvert 30. minutt
- Viser linje, destinasjon og minutter til avgang

## Slik bygger du appen

### Krav
- Android Studio Hedgehog (2023.1.1) eller nyere
- JDK 11 eller nyere
- Android SDK 26+

### Steg
1. Åpne Android Studio
2. Velg **File → Open** og pek på denne mappen (`BussWidget/`)
3. La Gradle synkronisere (kan ta 1-2 minutter første gang)
4. Koble til din Android-telefon med USB og aktiver **USB-feilsøking**
   - Innstillinger → Om telefonen → Trykk 7 ganger på Byggnummer
   - Innstillinger → Utvikleralternativer → USB-feilsøking
5. Trykk ▶ (Run) i Android Studio

### Eller installer via APK
1. I Android Studio: **Build → Build Bundle(s)/APK(s) → Build APK(s)**
2. APK-filen finner du i `app/build/outputs/apk/debug/`
3. Overfør til telefonen og installer

## Slik bruker du widgeten
1. Trykk og hold på hjemskjermen
2. Velg **Widgets**
3. Finn **BussWidget** (velg størrelse: Liten / Medium / Stor)
4. Dra widgeten til skjermen
5. Søk etter din holdeplass og trykk **Lagre**

## Prosjektstruktur
```
app/src/main/java/no/busswidget/
├── api/
│   └── EnturApi.kt          # Entur GraphQL + Geocoder API
├── data/
│   └── WidgetPrefs.kt       # Lagrer innstillinger per widget
├── widget/
│   ├── BaseBussWidget.kt    # Felles widget-logikk
│   ├── SmallBussWidget.kt   # 2×1 widget
│   ├── MediumBussWidget.kt  # 4×1 widget
│   ├── LargeBussWidget.kt   # 4×2 widget
│   └── WidgetUpdateService.kt
└── ui/
    ├── MainActivity.kt       # Forside med instruksjoner
    └── WidgetConfigActivity.kt # Søk og lagre holdeplass
```

## API
Bruker [Entur Journey Planner API](https://developer.entur.org/) – gratis og åpent for alle.
Ingen API-nøkkel nødvendig. Data oppdateres automatisk hvert 30. minutt.
