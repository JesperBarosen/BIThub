# Tester for REST-serveren

Her beskriver vi hvordan vi tester REST-serveren i `springboot/restserver`, og hvordan testene for REST-klienten i ui skal kjøre mot en ekte server. Alle testene kjører med `mvn clean verify` fra `bithub/`, uten at noen må starte serveren først.

## 1. Tester i `springboot/restserver`

| Testklasse | Type | Hva den tester |
|---|---|---|
| [`CourseControllerTest`](../../bithub/springboot/restserver/src/test/java/rest/api/CourseControllerTest.java) | `MockMvc` med falsk `ReviewService` | `GET /api/courses` og `GET /api/courses/{code}`: JSON-feltene, gjennomsnitt, visningsnavn og `authorType`, at e-posten til eieren aldri sendes ut, 400 og 404 |
| [`ReviewControllerTest`](../../bithub/springboot/restserver/src/test/java/rest/api/ReviewControllerTest.java) | `MockMvc` med falsk `ReviewService` | `POST /api/courses/{code}/reviews`: gyldig anmeldelse gir 201, ugyldig vurdering og ugyldig JSON gir 400, ukjent emne gir 404 uten at noe lagres |
| [`UserControllerTest`](../../bithub/springboot/restserver/src/test/java/rest/api/UserControllerTest.java) | `MockMvc` med falske `UserService`, `ReviewService` og `TokenService` | `POST /api/users`, `POST /api/login`, `PUT /api/users/{email}/password` og `GET /api/me/reviews`: token i svaret, at passord, hash og salt aldri sendes ut, 204, 400, 401 og 409 |
| [`TokenServiceTest`](../../bithub/springboot/restserver/src/test/java/rest/auth/TokenServiceTest.java) | Vanlig enhetstest | At hvert token er unikt og hører til riktig bruker, og at ukjente eller manglende tokener ikke gir noen bruker |
| [`RestIntegrationTest`](../../bithub/springboot/restserver/src/test/java/rest/RestIntegrationTest.java) | `@SpringBootTest` med ekte server og HTTP | Hele serveren startes på en ledig port med ekte tjenester i core og ekte lagring i file. Testene sender HTTP-forespørsler med `java.net.http.HttpClient` og sjekker både svaret og innholdet i datafilene, blant annet samtidig registrering og passordbytte |
| [`RemoteClientIntegrationTest`](../../bithub/springboot/restserver/src/test/java/rest/RemoteClientIntegrationTest.java) | `@SpringBootTest` med ekte server og ekte klient | `RemoteReviewRepository` fra ui henter emner, sender inn og henter en anmeldelse, og får feilmeldingen fra serveren for ukjent emne. Testen sjekker også at JSON-feltene stemmer med `rest-api.md` (#180) |
| [`RestApplicationTest`](../../bithub/springboot/restserver/src/test/java/rest/RestApplicationTest.java) | Start av Spring-konteksten | At serveren starter, og at `ReviewService` og `UserService` skriver til filene som er satt med `bithub.reviews-file` og `bithub.users-file` |

`MockMvc`-testene tester bare kontrollerne og feilhåndteringen. De er raske og viser presist hvilken del som feiler. `RestIntegrationTest` viser at delene virker sammen: HTTP, JSON, kontrollerne, tjenesten i core og lagringen i file.

Feilsvar med `401` testes i `UserControllerTest`, for feil innlogging og egne anmeldelser uten gyldig token (#174). `403` finnes ikke ennå, og testes i #176 og #177, sammen med endepunktene som bruker dem.

### Testdekning

JaCoCo lager en rapport for modulen i `bithub/springboot/restserver/target/site/jacoco/index.html` når `mvn clean verify` kjøres. Etter #154 dekker testene 98 % av instruksjonene. Det eneste som ikke kjøres, er `main`-metoden i `RestApplication`.

## 2. Midlertidige filer

Ingen tester skriver til `~/.bithub`. Integrasjonstestene setter filstiene med Spring-innstillingene `bithub.reviews-file` og `bithub.users-file`, og peker dem til en mappe fra JUnit sin `@TempDir`:

```java
@TempDir
static Path tempDir;

@DynamicPropertySource
static void useTemporaryFiles(DynamicPropertyRegistry registry) {
    registry.add("bithub.reviews-file", () -> tempDir.resolve("courses.json").toString());
    registry.add("bithub.users-file", () -> tempDir.resolve("users.json").toString());
}
```

## 3. Tester for REST-klienten

REST-klienten testes på to måter (#180). Ingen av testene trenger at serveren startes manuelt.

| Testklasse | Type | Hva den tester |
|---|---|---|
| [`RemoteReviewRepositoryTest`](../../bithub/ui/src/test/java/ui/RemoteReviewRepositoryTest.java) (ui) | Falsk server | Klienten mot en falsk server (`HttpServer` fra JDK) på en ledig port: forespørslene klienten sender, at svarene leses riktig, feilsvar (400, 401, 403, 404 og 500), ugyldig JSON, manglende felt, at serveren er nede, forsinkede svar og tidsavbrudd |
| [`RemoteClientIntegrationTest`](../../bithub/springboot/restserver/src/test/java/rest/RemoteClientIntegrationTest.java) (springboot/restserver) | Ekte server | At klient og server er enige om JSON-formatet, se avsnitt 1 |
<br>

- **Tidsavbrudd testes uten å vente lenge.** `RemoteReviewRepository` har en konstruktør med tidsgrense som bare testene i pakken kan bruke. Testen setter grensen til 200 ms, og den falske serveren venter med å svare.
- **401 og 403 gir meldingen fra serveren.** Klienten kaster `IllegalArgumentException` med meldingen, slik den gjør for 400 og 404, så skjermene kan vise den til brukeren.
- **Testen med ekte klient ligger i `springboot/restserver`.** Modulen ui skal ikke avhenge av serveren, så `springboot/restserver` har `ui` som avhengighet med `<scope>test</scope>`. Serveren startes med `@SpringBootTest(webEnvironment = RANDOM_PORT)`, og dataene lagres i midlertidige filer, slik det er beskrevet i avsnitt 2.
- **TestFX-testene i ui bruker [`InMemoryReviewRepository`](../../bithub/ui/src/test/java/ui/InMemoryReviewRepository.java)** (#175), en falsk lagring i minnet, og sender ingen HTTP-forespørsler.

Når innlogging er koblet til klienten (#176, #178 og #179), utvides `RemoteClientIntegrationTest` med registrering og innlogging, og HTTP-klienten for brukere får tester med falsk server på samme måte som `RemoteReviewRepositoryTest`.

## 4. Nye endepunkter

Endepunktene som kommer i sprint 2, får egne `MockMvc`-tester i samme issue som lager endepunktet, og bør legges til i `RestIntegrationTest` når de påvirker lagringen.