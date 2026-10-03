
# Gr2610 - IT1901 prosjekt

[open in Eclipse Che](https://che.iaas.ntnu.no/#https://git.ntnu.no/IT1901-2026-groups/gr2610?new)

Dette er kodelageret til gruppe 2610 i faget IT1901 ved NTNU. Appen lar studenter anmelde emner
med terningkast 1-5 og kommentar, og viser gjennomsnittlig vurdering per emne.

## Innhold i repoet

Kodingsprosjektet ligger i mappen [`bithub/`](bithub/) og består av tre moduler:

- **`core/`** – domenelogikk
- **`file/`** – persistens (lagring til JSON)
- **`ui/`** – brukergrensesnitt i JavaFX

`ui` og `file` avhenger av `core`, men `core` avhenger ikke av noen av dem

Se [denne filen](bithub/ui/README.md) for beskrivelse av appen og dens funksjonalitet,
skjermbilder og brukerhistorier.

## Nødvendige verktøy og avhengigheter

- **Java 17** eller nyere
- **Maven 3.9** eller nyere
- **JavaFX 21**
- **JUnit 5**
- **JaCoCo**

Alle avhengigheter lastes ned automatisk av Maven ved bygging.

## Bygge, teste og kjøre

Kommandoene kjøres fra `bithub/`-mappa:

```python 
cd bithub
mvn clean install   # bygger alle moduler
mvn test             # kjører testene
mvn verify            # kjører tester, kodekvalitetssjekker, genererer testdekningsrapport
mvn javafx:run       # starter appen
```

Testdekningsrapporten fra JaCoCo finnes etterpå i `bithub/core/target/site/jacoco/index.html`,
`bithub/file/target/site/jacoco/index.html` og `bithub/ui/target/site/jacoco/index.html`.