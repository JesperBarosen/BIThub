# Gr2610 - IT1901 prosjekt

[open in Eclipse Che](https://che.iaas.ntnu.no/#https://git.ntnu.no/IT1901-2026-groups/gr2610?new)

Dette er kodelageret til gruppe 2610 i faget IT1901 ved NTNU. Vi lager appen BIThub, der studenter kan anmelde emner
med terningkast 1-5 og kommentar.

I nåværende versjon kan man velge emne og sende inn en anmeldelse, og lese anmeldelsene for et emne. Appen er delt i en JavaFX-klient og en REST-server. Klienten henter og sender alle data til serveren, og serveren lagrer anmeldelsene i en JSON-fil.

Se [bithub/README.md](bithub/README.md) for beskrivelse av appen og dens funksjonalitet, skisser fra Figma og brukerhistorier.

## Arkitektur

Kodingsprosjektet ligger i mappen [`bithub/`](bithub/) og består av fire moduler: tre lag (core, file og ui) og REST-serveren. Hver modul er en egen Maven-modul, og core, file og ui er i tillegg egne Java-moduler (`module-info.java`), slik at avhengighetene mellom lagene er eksplisitte.

- **core** (`bithub.core`) er domenelaget og er ikke avhengig av noen av de andre modulene.
- **file** (`bithub.file`) er persistenslaget. Det bruker domeneklassene fra `core`, implementerer [`ReviewRepository`](bithub/core/src/main/java/core/repository/ReviewRepository.java) og gjør dataene om til og fra JSON med Jackson.
- **ui** (`bithub.ui`) er brukergrensesnittlaget. Det bruker `core` for domeneobjektene og [`ReviewService`](bithub/core/src/main/java/core/service/ReviewService.java), og henter dataene fra REST-serveren med [`RemoteReviewRepository`](bithub/ui/src/main/java/ui/client/RemoteReviewRepository.java). `ui` er ikke avhengig av `file`.
- **springboot/restserver** (`rest`) er REST-serveren. Den bruker `core` og `file`, og er den eneste som leser og skriver JSON-filene.

### Pakkediagram

![Pakkediagram for BIThub](arkitektur/png/pakkeDiagram.png)

Kildefilen til diagrammet ligger i [`arkitektur/pakkeDiagram.puml`](arkitektur/pakkeDiagram.puml).

Pilene viser avhengighetene mellom modulene: `ui` er avhengig av `core`, JavaFX, Jackson og
`java.net.http`, men ikke av `file`. `ui` snakker med [`springboot/restserver`](bithub/springboot/restserver) over HTTP. Serveren
er avhengig av `core`, `file` og Spring Boot, `file` er avhengig av `core` og Jackson, og `core` er
ikke avhengig av noen av de andre modulene.

### Klassediagram for core

![Klassediagram for core](arkitektur/png/klassediagramCore.png)

Kildefilen til diagrammet ligger i [`arkitektur/klassediagramCore.puml`](arkitektur/klassediagramCore.puml).

- Et [`Course`](bithub/core/src/main/java/core/model/Course.java) har en liste med [`Review`](bithub/core/src/main/java/core/model/Review.java), og regner ut gjennomsnittlig vurdering.
- [`ReviewService`](bithub/core/src/main/java/core/service/ReviewService.java) validerer og sender anmeldelser videre, men kjenner bare til grensesnittet
  [`ReviewRepository`](bithub/core/src/main/java/core/repository/ReviewRepository.java). Slik er ikke `core` avhengig av hvordan dataene lagres.

### Klassediagram for file

![Klassediagram for file](arkitektur/png/klassediagramFile.png)

Kildefilen til diagrammet ligger i [`arkitektur/klassediagramFile.puml`](arkitektur/klassediagramFile.puml).

- [`ReviewHandler`](bithub/file/src/main/java/file/ReviewHandler.java) og [`UserHandler`](bithub/file/src/main/java/file/UserHandler.java) implementerer [`DataHandler`](bithub/file/src/main/java/file/storage/DataHandler.java) og leser og skriver JSON med Jackson.
- [`ReviewHandler`](bithub/file/src/main/java/file/ReviewHandler.java) implementerer også [`ReviewRepository`](bithub/core/src/main/java/core/repository/ReviewRepository.java) fra `core`, og gjør om mellom
  domeneklassene ([`Course`](bithub/core/src/main/java/core/model/Course.java), [`Review`](bithub/core/src/main/java/core/model/Review.java)) og objektene som lagres i filen ([`CourseDTO`](bithub/file/src/main/java/file/dto/CourseDTO.java), [`ReviewDTO`](bithub/file/src/main/java/file/dto/ReviewDTO.java)).

### REST-API

Appen deles i en REST-server (modulen [`springboot/restserver`](bithub/springboot/restserver)) og JavaFX-klienten. Alle forespørslene serveren støtter, med eksempler på forespørsel og svar, statuskoder og innlogging, er beskrevet i [docs/release3/rest-api.md](docs/release3/rest-api.md).

## Mappestruktur i kodingsprosjektet

```text
└───bithub
    ├───pom.xml
    │    -- Foreldre-POM som samler modulene core, file, ui og springboot/restserver.
    ├───README.md
    │    -- Beskrivelse av appen, skisser fra Figma og brukerhistorier.
    │
    ├───core
    │   └───src
    │       ├───main
    │       │   └───java
    │       │       └───core
    │       │           -- Course, Review og User står for domenelogikken og validering.
    │       │           -- ReviewService og UserService håndterer anmeldelser og brukere.
    │       │           -- ReviewRepository og UserRepository er grensesnittene for lagring.
    │       └───test
    │           └───java
    │               └───core
    │                   -- Testene for core-modulen ligger her.
    │
    ├───file
    │   └───src
    │       ├───main
    │       │   └───java
    │       │       └───file
    │       │           -- ReviewHandler og UserHandler leser og skriver JSON med Jackson.
    │       │           -- JsonFileStorage sørger for trygg skriving når flere forespørsler kommer samtidig.
    │       │           -- CourseDTO, ReviewDTO og UserDTO er objektene som lagres til fil.
    │       └───test
    │           └───java
    │               └───file
    │                   -- Testene for file-modulen ligger her.
    │
    ├───springboot
    │   └───restserver
    │       └───src
    │           ├───main
    │           │   ├───java
    │           │   │   └───rest
    │           │   │       -- RestApplication starter Spring Boot-serveren.
    │           │   │       ├───api
    │           │   │       │   -- Kontrollerne med endepunktene i REST-API-et, og felles feilhåndtering.
    │           │   │       └───model
    │           │   │           -- Objektene som tas imot og sendes som JSON i forespørsler og svar.
    │           │   └───resources
    │           │       -- application.properties med innstillingene til serveren.
    │           └───test
    │               └───java
    │                   └───rest
    │                       -- Testene for REST-serveren, med MockMvc og @SpringBootTest.
    │
    └───ui
        ├───images
        │    -- Skisser fra Figma som brukes i bithub/README.md.
        │
        └───src
            ├───main
            │   ├───java
            │   │   └───ui
            │   │       -- App, Navigation og kontrollerne ligger her.
            │   │       -- RemoteReviewRepository henter og sender data til REST-serveren.
            │   └───resources
            │       └───ui
            │           -- FXML-filene og Style.css for brukergrensesnittet ligger her.
            │
            └───test
                └───java
                    └───ui
                        -- Testene for ui-modulen ligger her, skrevet med TestFX.
```

## Nødvendige verktøy og avhengigheter

- **Java 17** eller nyere
- **Maven 3.9** eller nyere
- **JavaFX 21**
- **JUnit 5**
- **JaCoCo**
- **Checkstyle** og **SpotBugs** for kodekvalitet
- **Jackson**
- **TestFX**
- **Spring Boot 3.4** for REST-serveren
- **Mockito**

Alle avhengigheter bortsett fra Java og Maven lastes ned automatisk av Maven ved bygging.

## Bygge, teste og kjøre

Kommandoene kjøres fra `bithub/`-mappen:

- `mvn clean install` - bygger alle moduler
- `mvn test` - kjører testene
- `mvn verify` - kjører tester, kodekvalitetssjekker og genererer testdekningsrapport

Appen trenger at REST-serveren kjører. Start serveren i én terminal fra `bithub/`:

```bash
mvn install -DskipTests
mvn -pl springboot/restserver spring-boot:run
```

`-DskipTests` hopper over testene slik at oppstarten går raskere.

Start så appen i en ny terminal, også fra `bithub/`:

```bash
mvn javafx:run
```

Serveren kjører på `http://localhost:8080`, og lagrer dataene i `~/.bithub/`. 

Testdekningsrapporten fra JaCoCo finnes etterpå i `target/site/jacoco/index.html` i hver modul, for eksempel
`bithub/core/target/site/jacoco/index.html` og `bithub/springboot/restserver/target/site/jacoco/index.html`.