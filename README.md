
# Gr2610 - IT1901 prosjekt

[open in Eclipse Che](https://che.iaas.ntnu.no/#https://git.ntnu.no/IT1901-2026-groups/gr2610?new)

Dette er kodelageret til gruppe 2610 i faget IT1901 ved NTNU. Appen lar studenter anmelde emner
med terningkast 1-5 og kommentar, og viser gjennomsnittlig vurdering per emne.

## Innhold i repoet

Kodingsprosjektet ligger i rota av kodelageret og består av `pom.xml`, `core/` og `ui/`.

- `core/src/main/java/core` - domenelogikk (`Course`, `Review`) og persistens
- `docs/` - dokumentasjon per release 
- `ui/src/main/java/ui` - brukergrensesnitt i JavaFX (`App`, `AppController`, `App.fxml`)
- `config/` - konfigurasjon for kodekvalitetsverktøy (Checkstyle og SpotBugs)


## Nødvendige verktøy og avhengigheter

- **Java 17** eller nyere
- **Maven 3.9** eller nyere
- **JavaFX 21** 
- **JUnit 5**
- **JaCoCo** 

Alle avhengigheter lastes ned automatisk av Maven ved bygging.


## Bygge, teste og kjøre

Kommandoene kjøres fra rota av kodelageret:

- `mvn clean install` - bygger alle moduler
- `mvn test` - kjører testene
- `mvn verify` - kjører tester, kodekvalitetssjekker og genererer testdekningsrapport

Testdekningsrapporten fra JaCoCo finnes etterpå i `core/target/site/jacoco/index.html` 
og `ui/target/site/jacoco/index.html`.

Appen startes fra `ui`-modulen:

    mvn javafx:run -f ui/pom.xml

