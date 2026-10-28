# Tester for REST-serveren

Her beskriver vi hvordan vi tester REST-serveren i `springboot/restserver`, og hvordan REST-klienten i ui testes mot en falsk og en ekte server. Alle testene kjører med `mvn clean verify` fra `bithub/`, uten at noen må starte serveren først.

## 1. Tester i `springboot/restserver`

| Testklasse | Type | Hva den tester |
|---|---|---|
| `CourseControllerTest` | `MockMvc` med falsk `ReviewService` | `GET /api/courses` og `GET /api/courses/{code}`: JSON-feltene, gjennomsnitt, visningsnavn og `authorType`, at e-posten til eieren aldri sendes ut, 400 og 404 |
| `ReviewControllerTest` | `MockMvc` med falsk `ReviewService` | `POST /api/courses/{code}/reviews`: gyldig anmeldelse gir 201, ugyldig vurdering og ugyldig JSON gir 400, ukjent emne gir 404 uten at noe lagres |
| `UserControllerTest` | `MockMvc` med falske `UserService`, `ReviewService` og `TokenService` | `POST /api/users`, `POST /api/login`, `PUT /api/users/{email}/password` og `GET /api/me/reviews`: token i svaret, at passord, hash og salt aldri sendes ut, 204, 400, 401 og 409 |
| `TokenServiceTest` | Vanlig enhetstest | At hvert token er unikt og hører til riktig bruker, og at ukjente eller manglende tokener ikke gir noen bruker |
| `RestIntegrationTest` | `@SpringBootTest` med ekte server og HTTP | Hele serveren startes på en ledig port med ekte tjenester i core og ekte lagring i file. Testene sender HTTP-forespørsler med `java.net.http.HttpClient` og sjekker både svaret og innholdet i datafilene, blant annet samtidig registrering og passordbytte |
| `RemoteClientIntegrationTest` | `@SpringBootTest` med ekte server og klientene fra ui | `RemoteReviewRepository` og `RemoteAuthenticationClient` snakker med hele serveren: registrering, innlogging, anmeldelser med token, anonyme og egne anmeldelser, endring og sletting, passordbytte, utlogging, gjester som avvises med `401` og JSON-kontrakten fra `rest-api.md` (#180) |
| `RestApplicationTest` | Start av Spring-konteksten | At serveren starter, og at `ReviewService` og `UserService` skriver til filene som er satt med `bithub.reviews-file` og `bithub.users-file` |

`MockMvc`-testene tester bare kontrollerne og feilhåndteringen. De er raske og viser presist hvilken del som feiler. `RestIntegrationTest` viser at delene virker sammen: HTTP, JSON, kontrollerne, tjenesten i core og lagringen i file.

Feilsvar med `401` testes i `UserControllerTest`, `ReviewControllerTest` og `OwnReviewControllerTest`, for feil innlogging og forespørsler uten gyldig token. `403` testes i `OwnReviewControllerTest` og `RestIntegrationTest`, for endring og sletting av andres anmeldelser (#177) og passordbytte for en annen bruker (#176).

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

## 3. Avtale for testene av REST-klienten

Testene for REST-klienten lages i sprint 2 (#180). Vi har avtalt dette for testen der en ekte klient snakker med en ekte server:

- **Serveren startes av testen.** Testen bruker `@SpringBootTest(webEnvironment = RANDOM_PORT)`, slik som `RestIntegrationTest`. Spring starter serveren på en ledig port før testen og stopper den etterpå, så ingen må starte serveren manuelt, og testen kolliderer ikke med en server som allerede kjører på 8080.
- **Klienten får adressen fra testen.** `RemoteReviewRepository` (#172) må kunne lages med en serveradresse, for eksempel `new RemoteReviewRepository(URI.create("http://localhost:" + port))`. Porten hentes med `@LocalServerPort`.
- **Testen ligger i `springboot/restserver`.** Modulen ui skal ikke avhenge av serveren, så den gjennomgående testen legges i `springboot/restserver/src/test/java/rest/`. Modulen får `ui` som avhengighet med `<scope>test</scope>`, slik at bare testene kan bruke klienten.
- **Data lagres i midlertidige filer,** slik det er beskrevet i avsnitt 2.
- **Testen sjekker JSON-kontrakten fra begge sider.** Klienten sender inn en anmeldelse og henter den tilbake, og testen sjekker at feltene stemmer med `rest-api.md`. Når innlogging er på plass (#176), utvides testen med registrering og innlogging.
- **`MockMvc`-testene i `springboot/restserver` og testene i ui med falsk server eller falsk `HttpClient` blir i tillegg.** Den gjennomgående testen erstatter dem ikke, men viser at klient og server faktisk passer sammen.

## 4. Tester av REST-klienten i ui

Klientene i ui testes mot en falsk server laget med `com.sun.net.httpserver.HttpServer` på en ledig port. Den falske serveren svarer med de statuskodene og den JSON-en testen ber om, og husker forespørselen den fikk.

| Testklasse | Hva den tester |
|---|---|
| [`RemoteReviewRepositoryTest`](../../bithub/ui/src/test/java/ui/client/RemoteReviewRepositoryTest.java) | Forespørslene og JSON-en klienten sender, at tokenet sendes, 400, 401, 403, 404 og 500, forsinket svar, tidsavbrudd, ugyldig JSON, manglende felt og at serveren ikke svarer |
| [`RemoteAuthenticationClientTest`](../../bithub/ui/src/test/java/ui/client/RemoteAuthenticationClientTest.java) | Registrering, innlogging, passordbytte og utlogging: JSON, token i headeren, kodet e-post i stien, 400, 401, 403, 409 og 500, og at serveren ikke svarer |
| [`RemoteClientIntegrationTest`](../../bithub/springboot/restserver/src/test/java/rest/RemoteClientIntegrationTest.java) | De samme klientene mot den ekte serveren, se avsnitt 1 |

Klienten gjør om feilsvar slik at skjermene kan vise en melding:

- `400`, `403` og `404` gir `IllegalArgumentException` med meldingen fra serveren. Ved endring og sletting gir `403` i stedet `NotReviewOwnerException`, og `404` gir `false`.
- `401` gir `IllegalArgumentException` med «Du er ikke lenger logget inn. Logg inn på nytt.». Det skjer for eksempel når serveren har startet på nytt og glemt tokenet.
- Andre feilkoder, ugyldig JSON, manglende felt, nettverksfeil og tidsavbrudd gir `UncheckedIOException`.

`RemoteReviewRepository` har en konstruktør med egen tidsgrense som bare testene bruker, slik at tidsavbrudd kan testes på 200 ms i stedet for ti sekunder.

TestFX-testene trenger ingen server. De bruker `InMemoryReviewRepository` og falske autentiseringsklienter i testmappen til ui.

## 5. Nye endepunkter

Endepunktene som kommer i sprint 2, får egne `MockMvc`-tester i samme issue som lager endepunktet, og bør legges til i `RestIntegrationTest` når de påvirker lagringen.