# Gr2610 - IT1901 prosjekt

[open in Eclipse Che](https://che.iaas.ntnu.no/#https://git.ntnu.no/IT1901-2026-groups/gr2610?new)

Dette er kodelageret til gruppe 2610 i faget IT1901 ved NTNU. Vi lager BIThub, en app der studenter kan anmelde emner
med en til fem stjerner og en kommentar.

## Innhold

- [Funksjonalitet](#funksjonalitet)
- [Kom i gang](#kom-i-gang)
- [Dokumentasjon](#dokumentasjon)
- [Arkitektur](#arkitektur)
- [Mappestruktur](#mappestruktur)

## Funksjonalitet

| Hvem | Hva de kan gjøre |
|---|---|
| Alle | Søke opp emner, lese anmeldelser med gjennomsnittlig vurdering og antall anmeldelser, og åpne infosiden |
| Gjester | Fortsette uten konto, og logge inn eller registrere seg senere |
| Innloggede brukere | Skrive en anmeldelse per emne, med navn eller anonymt, og se, endre og slette egne anmeldelser under «Mine anmeldelser» |
| Innloggede brukere | Se navn og e-post på profilsiden, endre navn og passord, og logge ut |

I release 3 delte vi appen i en REST-server og en JavaFX-klient, og utvidet appen med brukerkontoer, gjestemodus,
emnesøk, stjerner og «Mine anmeldelser». Klienten henter og sender alle data over HTTP, og bare serveren leser og skriver
JSON-filene.

Se [bithub/README.md](bithub/README.md) for en beskrivelse av appen, skisser fra Figma og brukerhistorier.

## Kom i gang

### Verktøy

| Verktøy | Versjon | Merknad |
|---|---|---|
| JDK | 17 eller nyere | Må installeres selv. Bruk JDK 21 hvis du skal lage installasjonsprogrammet. |
| Maven | 3.9 eller nyere | Må installeres selv |
| WiX Toolset | 3.14 | Bare for å lage installasjonsprogrammet på Windows |

Maven laster ned resten ved bygging: JavaFX 21, ControlsFX 11.2, Spring Boot 3.4, Jackson, JUnit 5, TestFX,
Mockito, JaCoCo, Checkstyle og SpotBugs.

### Kjøre lokalt

Appen trenger at REST-serveren kjører. Start serveren i en terminal fra `bithub/`:

```bash
mvn install -DskipTests
mvn -pl springboot/restserver spring-boot:run
```

`-DskipTests` hopper over testene, men Checkstyle og SpotBugs kjører fortsatt.

Start så appen i en ny terminal, også fra `bithub/`:

```bash
mvn javafx:run
```

Serveren kjører på `http://localhost:8080` og lagrer dataene i `courses.json` og `users.json` under `~/.bithub/`.

Vil du prøve appen med ferdige data og testbrukere, står fremgangsmåten i
[docs/release3/release3.md](docs/release3/release3.md#eksempeldata).

### Kjøre i Eclipse Che

1. Åpne lenken «open in Eclipse Che» øverst i denne filen.
2. Kjør «1. Bygg og test (mvn clean verify)».
3. Kjør «2. Start REST-serveren», og vent til serveren har startet.
4. Kjør «3. Start JavaFX-appen».
5. Åpne endepunktet `01-desktop-ui` for å se appen i nettleseren.

Kommandoene er definert i [`devfile.yaml`](devfile.yaml).

### Bygge og teste

Kommandoene kjøres fra `bithub/`:

| Kommando | Hva den gjør |
|---|---|
| `mvn test` | Kjører testene og lager testdekningsrapport |
| `mvn verify` | Gjør det samme, og kjører i tillegg Checkstyle og SpotBugs |
| `mvn clean install` | Bygger alt på nytt, kjører alle kontrollene og installerer modulene lokalt |

TestFX-testene åpner vinduer, så ikke bruk mus og tastatur mens de kjører.

Testdekningsrapporten ligger i `target/site/jacoco/index.html` i hver modul, for eksempel
`bithub/core/target/site/jacoco/index.html`.

Hva kontrollene sjekker, står i [docs/release3/code-quality.md](docs/release3/code-quality.md).

### Lage et ferdig produkt

Hvordan du bygger serveren som en kjørbar JAR-fil og lager et installasjonsprogram for klienten med jlink og jpackage,
står i [bithub/README.md](bithub/README.md#bygging-installasjon-og-oppstart).

## Dokumentasjon

Dokumentasjonen for hver innlevering ligger i egen mappe under `docs/`:

- [Release 1](docs/release1/release1.md)
- [Release 2](docs/release2/release2.md)
- [Release 3](docs/release3/release3.md)

Tekniske dokumenter for release 3:

- [REST-API](docs/release3/rest-api.md)
- [Tester for REST-serveren](docs/release3/rest-tester.md)
- [Pakkestruktur](docs/release3/pakkestruktur.md)
- [Kodekvalitet](docs/release3/code-quality.md)
- [Persistens](docs/release3/persistence.md)
- [JavaFX og asynkrone kall](docs/release3/javafx.md)

Prosess og refleksjon for release 3:

- [Arbeidsflyt](docs/release3/workflow.md)
- [Samarbeid i teamet](docs/release3/teamwork.md)
- [Bidrag fra hvert medlem](docs/release3/contribution.md)
- [De største utfordringene](docs/release3/challenges.md)
- [Bærekraft](docs/release3/sustainability.md)
- [Bruk av KI-verktøy](docs/release3/ai-tools.md)

## Arkitektur

Kodingsprosjektet ligger i [`bithub/`](bithub/) og består av fire Maven-moduler. `core`, `file` og `ui` er i tillegg
egne Java-moduler (`module-info.java`), slik at avhengighetene mellom lagene er eksplisitte.

| Modul | Ansvar | Avhengig av |
|---|---|---|
| `core` (`bithub.core`) | Domeneklasser, validering, tjenester og grensesnitt for lagring | Ingen av de andre modulene |
| `file` (`bithub.file`) | Implementerer [`ReviewRepository`](bithub/core/src/main/java/core/repository/ReviewRepository.java) og [`UserRepository`](bithub/core/src/main/java/core/repository/UserRepository.java) med JSON-filer | `core`, Jackson |
| `ui` (`bithub.ui`) | JavaFX-klienten, som henter data fra serveren med [`RemoteReviewRepository`](bithub/ui/src/main/java/ui/client/RemoteReviewRepository.java) | `core`, JavaFX, ControlsFX, Jackson, `java.net.http` |
| `springboot/restserver` (`rest`) | REST-serveren, den eneste som leser og skriver JSON-filene | `core`, `file`, Spring Boot |

Serverens tester bruker også `ui`, slik at klienten kan testes mot den ekte serveren i
[`RemoteClientIntegrationTest`](bithub/springboot/restserver/src/test/java/rest/RemoteClientIntegrationTest.java).
Serveren er ikke avhengig av `ui` når den kjører.

### REST-arkitektur

Klienten og serveren kjører som to separate programmer. Ingen av kontrollerne i `ui` kjenner `Remote*`-klassene. De
bruker tjenester og grensesnitt, mens klassene i [`ui.client`](bithub/ui/src/main/java/ui/client) gjør kallene om til
HTTP-forespørsler.

- **Samme domenelogikk på begge sider:** [`ReviewService`](bithub/core/src/main/java/core/service/ReviewService.java)
  validerer anmeldelsen i klienten før den sendes. Serveren validerer den på nytt, fordi den ikke stoler på at klienten
  har sjekket noe.
- **Innlogging:** [`RemoteAuthenticationClient`](bithub/ui/src/main/java/ui/client/RemoteAuthenticationClient.java)
  sender innlogging og registrering til
  [`UserController`](bithub/springboot/restserver/src/main/java/rest/api/UserController.java). Serveren lager et token
  med [`TokenService`](bithub/springboot/restserver/src/main/java/rest/auth/TokenService.java) og sender det tilbake.
- **Token i klienten:** [`UserSession`](bithub/ui/src/main/java/ui/session/UserSession.java) tar vare på tokenet og gir
  det videre til [`RemoteReviewRepository`](bithub/ui/src/main/java/ui/client/RemoteReviewRepository.java), som sender
  det i headeren `Authorization` så lenge brukeren er innlogget. Passordbytte, navnebytte og utlogging går gjennom
  [`RemoteAuthenticationClient`](bithub/ui/src/main/java/ui/client/RemoteAuthenticationClient.java).
- **Bare serveren bruker `file`:** `ui` er ikke avhengig av `file`, så klienten kan ikke lese eller skrive filene selv.
  Kontrollerne på serveren bruker tjenestene i `core`, som lagrer gjennom
  [`ReviewHandler`](bithub/file/src/main/java/file/ReviewHandler.java) og
  [`UserHandler`](bithub/file/src/main/java/file/UserHandler.java).

Alle forespørslene serveren støtter, med eksempler, statuskoder og innlogging, er beskrevet i
[docs/release3/rest-api.md](docs/release3/rest-api.md).

### Pakkediagram

![Pakkediagram for BIThub](arkitektur/png/pakkeDiagram.png)

Kildefilen til diagrammet ligger i [`arkitektur/pakkeDiagram.puml`](arkitektur/pakkeDiagram.puml).

### Klassediagram for core

![Klassediagram for core](arkitektur/png/klassediagramCore.png)

Kildefilen til diagrammet ligger i [`arkitektur/klassediagramCore.puml`](arkitektur/klassediagramCore.puml).

- Et [`Course`](bithub/core/src/main/java/core/model/Course.java) har en liste med
  [`Review`](bithub/core/src/main/java/core/model/Review.java), og regner ut gjennomsnittlig vurdering.
- [`ReviewService`](bithub/core/src/main/java/core/service/ReviewService.java) validerer og sender anmeldelser videre,
  men kjenner bare grensesnittet [`ReviewRepository`](bithub/core/src/main/java/core/repository/ReviewRepository.java).
  Slik er ikke `core` avhengig av hvordan dataene lagres.

### Klassediagram for file

![Klassediagram for file](arkitektur/png/klassediagramFile.png)

Kildefilen til diagrammet ligger i [`arkitektur/klassediagramFile.puml`](arkitektur/klassediagramFile.puml).

- [`ReviewHandler`](bithub/file/src/main/java/file/ReviewHandler.java) og
  [`UserHandler`](bithub/file/src/main/java/file/UserHandler.java) implementerer
  [`DataHandler`](bithub/file/src/main/java/file/storage/DataHandler.java) og leser og skriver JSON med Jackson.
- [`ReviewHandler`](bithub/file/src/main/java/file/ReviewHandler.java) gjør om mellom domeneklassene
  ([`Course`](bithub/core/src/main/java/core/model/Course.java), [`Review`](bithub/core/src/main/java/core/model/Review.java))
  og objektene som lagres i filen ([`CourseDTO`](bithub/file/src/main/java/file/dto/CourseDTO.java),
  [`ReviewDTO`](bithub/file/src/main/java/file/dto/ReviewDTO.java)).

### Sekvensdiagrammer

Sekvensdiagrammer for brukerhistorie 1 og 2 ligger i [bithub/README.md](bithub/README.md#brukerhistorier).

## Mappestruktur

```text
└───bithub
    ├───pom.xml
    │    -- Foreldre-POM som samler modulene core, file, ui og springboot/restserver.
    ├───README.md
    │    -- Beskrivelse av appen, bygging og pakking, skisser fra Figma og brukerhistorier.
    │
    ├───core/src/main/java/core
    │   ├───model
    │   │   -- Course, Review, User og AuthorType med domenelogikk og validering.
    │   ├───service
    │   │   -- ReviewService og UserService håndterer anmeldelser og brukere.
    │   │   -- PasswordHasher hasher passord, og DefaultCourses har standardemnene.
    │   ├───repository
    │   │   -- ReviewRepository og UserRepository er grensesnittene for lagring.
    │   └───exception
    │       -- Unntak fra domeneregler, for eksempel DuplicateEmailException.
    │
    ├───file/src/main/java/file
    │   │   -- ReviewHandler og UserHandler leser og skriver JSON med Jackson.
    │   ├───dto
    │   │   -- CourseDTO, ReviewDTO og UserDTO er objektene som lagres til fil.
    │   └───storage
    │       -- DataHandler er grensesnittet for filhåndtering.
    │       -- JsonFileStorage sørger for trygg skriving når flere forespørsler kommer samtidig.
    │
    ├───springboot/restserver/src/main
    │   ├───java/rest
    │   │   │   -- RestApplication starter Spring Boot-serveren.
    │   │   ├───api
    │   │   │   -- Kontrollerne med endepunktene i REST-API-et, og felles feilhåndtering.
    │   │   ├───auth
    │   │   │   -- AuthenticationService og TokenService for innlogging og token.
    │   │   ├───exception
    │   │   │   -- Unntak som hører til en bestemt HTTP-status.
    │   │   └───model
    │   │       -- Objektene som tas imot og sendes som JSON i forespørsler og svar.
    │   └───resources
    │       -- application.properties med innstillingene til serveren.
    │
    └───ui
        ├───images
        │    -- Skisser fra Figma.
        └───src/main
            ├───java/ui
            │   │   -- App starter appen, og Navigation åpner skjermene.
            │   ├───controller
            │   │   -- En kontroller per skjerm.
            │   ├───client
            │   │   -- RemoteReviewRepository og RemoteAuthenticationClient snakker med REST-serveren.
            │   │   -- AuthenticationClient er grensesnittet kontrollerne bruker for innlogging.
            │   ├───session
            │   │   -- UserSession husker hvem som er logget inn.
            │   └───util
            │       -- BackgroundTaskRunner kjører serverkall i bakgrunnen.
            │       -- PasswordVisibility viser og skjuler passord.
            │       -- RatingStars viser vurderinger som stjerner med ControlsFX.
            └───resources/ui
                -- FXML-filene og Style.css for brukergrensesnittet.
```

Hver modul har testene sine under `src/test/java`, og de ligger stort sett i samme pakker som klassene de tester. Hva
hver pakke inneholder, og hvorfor vi delte det slik, står i [docs/release3/pakkestruktur.md](docs/release3/pakkestruktur.md).