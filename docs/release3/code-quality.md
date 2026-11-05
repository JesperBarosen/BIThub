# Kodekvalitet

Dette dokumentet beskriver hvordan vi sikrer kodekvaliteten i BIThub. Vi bruker automatiserte tester og kodeanalyse gjennom Maven for å oppdage feil og holde kodebasen konsistent.

BIThub består av fire Maven-moduler: `core`, `file`, `ui` og [`springboot/restserver`](../../bithub/springboot/restserver). Alle kontrollene i dette dokumentet gjelder alle fire.

## Testing

Tester sjekker at eksisterende funksjonalitet fortsetter å virke når prosjektet endres. Alle modulene har tester. Vi bruker JUnit 5 i alle modulene, TestFX for brukergrensesnittet i `ui`, Mockito der vi trenger å simulere feil eller tjenester, og Spring Boot Test i [`springboot/restserver`](../../bithub/springboot/restserver).

Vi tester hvert lag for seg, og har i tillegg tester som sjekker at lagene virker sammen. Ingen av testene trenger at serveren startes først.

| Modul | Hva som testes | Eksempler |
|---|---|---|
| `core` | Domeneklassene og tjenestene, med en falsk lagring | [`ReviewServiceTest`](../../bithub/core/src/test/java/core/service/ReviewServiceTest.java), [`UserServiceTest`](../../bithub/core/src/test/java/core/service/UserServiceTest.java), [`PasswordHasherTest`](../../bithub/core/src/test/java/core/service/PasswordHasherTest.java) |
| `file` | Lesing og skriving av JSON, feil ved lesing og skriving, og samtidige endringer | [`ReviewHandlerTest`](../../bithub/file/src/test/java/file/ReviewHandlerTest.java), [`UserHandlerTest`](../../bithub/file/src/test/java/file/UserHandlerTest.java), [`JsonFileStorageTest`](../../bithub/file/src/test/java/file/storage/JsonFileStorageTest.java) |
| `ui` | Kontrollerne med TestFX, bakgrunnskall og REST-klienten mot en falsk server | [`WriteReviewControllerTest`](../../bithub/ui/src/test/java/ui/controller/WriteReviewControllerTest.java), [`BackgroundTaskRunnerTest`](../../bithub/ui/src/test/java/ui/util/BackgroundTaskRunnerTest.java), [`RemoteReviewRepositoryTest`](../../bithub/ui/src/test/java/ui/client/RemoteReviewRepositoryTest.java) |
| [`springboot/restserver`](../../bithub/springboot/restserver) | Endepunktene med `MockMvc`, og hele serveren med ekte HTTP | [`ReviewControllerTest`](../../bithub/springboot/restserver/src/test/java/rest/api/ReviewControllerTest.java), [`UserControllerTest`](../../bithub/springboot/restserver/src/test/java/rest/api/UserControllerTest.java), [`RestIntegrationTest`](../../bithub/springboot/restserver/src/test/java/rest/RestIntegrationTest.java) |

### core

Domeneklassene og tjenestene testes med enhetstester. [`ReviewServiceTest`](../../bithub/core/src/test/java/core/service/ReviewServiceTest.java) og [`UserServiceTest`](../../bithub/core/src/test/java/core/service/UserServiceTest.java) bruker egne falske lagringer i stedet for ekte filer, slik at testene bare sjekker logikken i domenelaget. Det gjelder blant annet validering av vurderinger, at bare eieren kan endre og slette en anmeldelse, og at passord lagres som hash med salt.

### file

Lagringen testes for lesing, skriving og feiltilfeller, som tomme filer, ugyldig JSON og feil ved skriving. For å teste feil ved lesing og skriving bruker vi Mockito til å la `Files` kaste `IOException`, siden slike feil er vanskelige å fremprovosere med ekte filer. [`ReviewStorageTest`](../../bithub/file/src/test/java/file/ReviewStorageTest.java) sjekker at [`ReviewService`](../../bithub/core/src/main/java/core/service/ReviewService.java) og [`ReviewHandler`](../../bithub/file/src/main/java/file/ReviewHandler.java) virker sammen, og at en anmeldelse som sendes inn faktisk blir lagret til fil.

Serveren kan få flere forespørsler samtidig. Vi tester derfor at data ikke går tapt når flere tråder skriver til samme fil. [`ReviewHandlerTest`](../../bithub/file/src/test/java/file/ReviewHandlerTest.java) sender inn 20 anmeldelser samtidig, og endrer og sletter 20 anmeldelser samtidig (#149, #177). [`UserHandlerTest`](../../bithub/file/src/test/java/file/UserHandlerTest.java) og [`JsonFileStorageTest`](../../bithub/file/src/test/java/file/storage/JsonFileStorageTest.java) tester samtidig registrering og at fila ikke blir ødelagt hvis skrivingen feiler halvveis.

### ui

Brukergrensesnittet testes med TestFX. Testene sjekker hver kontroller, for eksempel feilmeldinger og innsending i [`WriteReviewController`](../../bithub/ui/src/main/java/ui/controller/WriteReviewController.java), visning av anmeldelser og gjennomsnitt i [`ReadReviewsController`](../../bithub/ui/src/main/java/ui/controller/ReadReviewsController.java), og navigasjonen mellom sidene i [`NewNavigationTest`](../../bithub/ui/src/test/java/ui/NewNavigationTest.java).

`ui` avhenger ikke av `file` (#175). UI-testene bruker derfor [`InMemoryReviewRepository`](../../bithub/ui/src/test/java/ui/client/InMemoryReviewRepository.java), en falsk lagring som holder emnene i minnet og oppfører seg som lagringen på serveren. Da tester UI-testene bare brukergrensesnittet, og de trenger verken filer eller en server som kjører. Kontrolltestene bruker også falske lagringer som svarer med forsinkelse eller feil, slik at vi kan teste lastetilstand og feilmeldinger.

[`BackgroundTaskRunnerTest`](../../bithub/ui/src/test/java/ui/util/BackgroundTaskRunnerTest.java) sjekker at REST-kall kjører på en bakgrunnstråd, at svaret kommer tilbake på JavaFX-tråden, og at et sent svar ignoreres etter at brukeren har navigert bort (#173). Mønsteret er beskrevet i [javafx.md](javafx.md).

[`RemoteReviewRepositoryTest`](../../bithub/ui/src/test/java/ui/client/RemoteReviewRepositoryTest.java) tester REST-klienten mot en falsk server som startes på en ledig port i testen. Testene sjekker forespørslene klienten sender, at svarene leses riktig, og at feilsvar og en server som er nede gir forståelige feil.

### springboot/restserver

`MockMvc`-testene tester hvert endepunkt med falske tjenester, inkludert statuskodene for feil (400, 401, 403, 404 og 409). De er raske og viser presist hvilken del som feiler. [`RestIntegrationTest`](../../bithub/springboot/restserver/src/test/java/rest/RestIntegrationTest.java) starter hele serveren på en ledig port med ekte tjenester og ekte lagring, og sender ekte HTTP-forespørsler. Den viser at HTTP, JSON, kontrollerne, tjenestene i `core` og lagringen i `file` virker sammen. Testene for serveren og klienten er beskrevet i detalj i [rest-tester.md](rest-tester.md).

### Felles regler for testene

- Tester som skriver til fil, bruker `@TempDir` fra JUnit, eller Spring-innstillingene `bithub.reviews-file` og `bithub.users-file` pekt til en midlertidig mappe. Testene skriver aldri til `~/.bithub`, og de påvirker ikke hverandre.
- Testene skal sjekke forventet oppførsel, grenseverdier og feilsituasjoner. For vurderinger tester vi for eksempel både gyldige grenseverdier (1 og 5) og verdier som 0 og 6, som skal avvises.
- Testene kjøres gjennom Maven, slik at kvalitetssikringen ikke avhenger av hvilken IDE et gruppemedlem bruker.

## Testdekning med JaCoCo

JaCoCo måler hvilke deler av koden som blir kjørt av testene. Vi bruker rapporten til å finne områder med for lite testing, slik at vi kan skrive flere tester der det trengs.

Vi bruker JaCoCo Maven Plugin versjon 0.8.12. `prepare-agent` kobler JaCoCo til testkjøringen, og `report` kjøres i `test`-fasen og lager rapporten. Rapportene lages for hver modul og åpnes med `index.html`:
- `bithub/core/target/site/jacoco/`
- `bithub/file/target/site/jacoco/`
- `bithub/ui/target/site/jacoco/`
- `bithub/springboot/restserver/target/site/jacoco/`

Vi har ikke satt en fast minimumsgrense for testdekning. En fast grense kan føre til at det skrives tester bare for å øke prosenten, i stedet for tester som sjekker faktisk oppførsel. JaCoCo brukes derfor som et analyseverktøy, og testdekningen vurderes i hver pull request. Nye klasser og metoder skal ha tester i samme pull request.

Høy testdekning er ikke et mål i seg selv. Testene skal først og fremst sjekke forventet oppførsel, grenseverdier og relevante feilsituasjoner. Når testene dekker dette for alle klassene, blir god testdekning en naturlig følge.

### Målt dekning i release 3

Tallene under er hentet fra JaCoCo etter en full kjøring av `mvn clean verify`
5. november 2026. Alle 639 tester bestod, uten feil eller tester som ble hoppet over.
Hele prosjektet ble kontrollert. Checkstyle rapporterte 0 brudd, og SpotBugs
rapporterte 0 funn.

| Modul | Instruksjonsdekning | Linjedekning | Branch-dekning | Tester |
| --- | --- | --- | --- | --- |
| `core` | 98,8 % | 98,4 % | 100,0 % | 169 |
| `file` | 98,9 % | 99,0 % | 96,0 % | 72 |
| `ui` | 96,3 % | 95,8 % | 91,2 % | 270 |
| `springboot/restserver` | 98,7 % | 98,7 % | 100,0 % | 128 |

Instruksjonsdekning viser hvor mange av instruksjonene i den kompilerte koden som
kjøres av testene. Linjedekning måler kildelinjer, mens branch-dekning måler
alternative utfall i betingelser, for eksempel begge veier i en `if`-setning.
Tallene gjelder hver modul samlet, ikke hver enkelt klasse.

I #206 brukte vi rapportene til å prioritere manglende feilsituasjoner og
kodeveier. Vi la blant annet til tester for hvert tomme felt separat i innlogging,
registrering og passordbytte, manglende kontrolleroppsett, tomme feilmeldinger og
gjentatte handlinger mens serverkall pågår. Vi testet også at mislykket endring
eller sletting beholder brukerens data, og at REST-klientene håndterer ugyldige
eller mangelfulle svar. Standardmetodene i `AuthenticationClient` testes for at
handlinger som ikke er støttet, avvises med en forklarende melding.

Emnesøkstestene bruker et tekstfelt der testen setter fokusstatusen. Da testes
åpning og lukking av nedtrekkslisten uavhengig av operativsystemets aktive vindu.

Det gjenstår udekkede kodeveier blant annet ved manglende elementer i listeceller,
bekreftelsesdialogen for sletting, feil ved lasting av FXML og håndtering av
avbrutte bakgrunnskall. Disse finnes i `ui.controller`, `ui` og `ui.util`.

## Checkstyle

Checkstyle sjekker at Java-koden følger en felles kodestil. Det gjør koden lettere å lese, og kode skrevet av forskjellige gruppemedlemmer følger de samme reglene.

Checkstyle er integrert i Maven og bruker regelsettet i [`config/checkstyle/checkstyle.xml`](../../config/checkstyle/checkstyle.xml). Regelsettet er basert på `google_checks.xml`, med innrykk på fire mellomrom. Vi valgte fire mellomrom fordi alle i gruppen er vant til det fra tidligere fag, og fordi det er standard innrykk for Java i VS Code. Ellers følger vi Googles regelsett, siden det er et etablert og gjennomarbeidet utgangspunkt.

Vi bruker Maven Checkstyle Plugin versjon 3.6.0 med Checkstyle versjon 10.26.1. Både produksjonskode og testkode sjekkes, slik at testene holder samme kvalitet som resten av koden. Checkstyle kjører målet `check` i `verify`-fasen, og kan også kjøres for seg fra `bithub/` med `mvn checkstyle:check`.

VS Code bruker samme regelsett gjennom `java.checkstyle.configuration` i [`.vscode/settings.json`](../../.vscode/settings.json). Rotmappen til repoet må åpnes i VS Code for at prosjektinnstillingene skal brukes.

Formateringen styres av [`.vscode/java-formatter.xml`](../../.vscode/java-formatter.xml), med fire mellomrom, mellomrom i stedet for tabulatorer og en linjelengde på 100 tegn. Det samkjører formateringen med Checkstyle-reglene. Automatisk formatering retter mange stilbrudd, men blant annet manglende Javadoc må vi fikse selv. HTML-formatering i Javadoc er slått av, fordi formatteren ellers flytter `<p>` til en egen linje, mens Checkstyle krever at `<p>` står rett foran teksten i avsnittet.

Med dagens oppsett rapporteres regelbrudd som warnings uten at bygget feiler. `BUILD SUCCESS` betyr derfor ikke at koden er uten Checkstyle-warnings. Vi har en egen regel om at en pull request ikke skal innføre nye Checkstyle-warnings. Den som oppretter pull requesten, sjekker Checkstyle-rapporten før review, og reviewer sjekker at det er gjort.

DTO-klassene i `file` ([`CourseDTO`](../../bithub/file/src/main/java/file/dto/CourseDTO.java), [`ReviewDTO`](../../bithub/file/src/main/java/file/dto/ReviewDTO.java) og [`UserDTO`](../../bithub/file/src/main/java/file/dto/UserDTO.java)) har `@SuppressWarnings("checkstyle:AbbreviationAsWordInName")`. «DTO» er en vanlig forkortelse, og navnene er lettere å kjenne igjen med store bokstaver.

## SpotBugs

SpotBugs gjør statisk analyse av Java-koden. Verktøyet leter etter mulige feil og problematiske mønstre som verken kompilatoren eller testene nødvendigvis oppdager.

Vi bruker SpotBugs Maven Plugin versjon 4.8.6.6. Målet `check` kjøres i `verify`-fasen. I motsetning til Checkstyle feiler bygget hvis SpotBugs finner feil. Vi bruker standardinnstillingene for analysenivå og terskel. SpotBugs lager en HTML-rapport, siden den er lettere å lese enn XML.

### Unntak

De fleste unntakene gjelder `EI_EXPOSE_REP2`. SpotBugs advarer når et objekt som kan endres, lagres i et felt i stedet for at det lages en kopi. For tjenester og lagringer er det meningen at flere klasser skal dele det samme objektet. En kopi ville for eksempel gitt to lagringer som ikke ser hverandres endringer.

Unntak som gjelder én klasse, legger vi direkte i koden med `@SuppressFBWarnings`, slik at begrunnelsen står ved siden av koden. Unntak som gjelder flere klasser, legger vi i [`config/spotbugs/exclude.xml`](../../config/spotbugs/exclude.xml).

**I [`config/spotbugs/exclude.xml`](../../config/spotbugs/exclude.xml):**

| Advarsel | Hvor | Begrunnelse |
|---|---|---|
| `EI_EXPOSE_REP2` | Feltet `reviewRepository` i [`core.service.ReviewService`](../../bithub/core/src/main/java/core/service/ReviewService.java) | [`ReviewService`](../../bithub/core/src/main/java/core/service/ReviewService.java) skal bruke den samme lagringen som den får utenfra, ikke en kopi. |
| `EI_EXPOSE_REP2` | Feltet `reviewRepository` i [`ui.Navigation`](../../bithub/ui/src/main/java/ui/Navigation.java) | Navigasjonen sender den samme lagringen videre til kontrollerne. |
| `EI_EXPOSE_REP2` | Feltet `reviewService` i klasser i `ui` og [`rest.api`](../../bithub/springboot/restserver/src/main/java/rest/api) | SpotBugs regner [`ReviewService`](../../bithub/core/src/main/java/core/service/ReviewService.java) som en klasse som kan endres, fordi den har metoden [`deleteReview`](../../bithub/core/src/main/java/core/service/ReviewService.java#L213) (#177). Tjenesten er delt, og kontrollerne skal bruke den direkte. |
| `EI_EXPOSE_REP2` | Feltet `userSession` i klasser i `ui` | Alle skjermene skal dele den samme sesjonen med den innloggede brukeren, ikke en kopi. Ellers ville en skjerm ikke sett at brukeren har logget inn eller ut (#178). |

**Med `@SuppressFBWarnings` i koden:**

| Advarsel | Hvor | Begrunnelse |
|---|---|---|
| `EI_EXPOSE_REP` | [`CourseDTO.getReviews()`](../../bithub/file/src/main/java/file/dto/CourseDTO.java#L88) | Jackson trenger direkte tilgang til listen for å lese og skrive JSON, og klassen brukes bare til lagring. |
| `EI_EXPOSE_REP2` | Konstruktøren til [`file.storage.JsonFileStorage`](../../bithub/file/src/main/java/file/storage/JsonFileStorage.java) | Deler handlerens `ObjectMapper` slik at lesing og skriving bruker samme JSON-konfigurasjon. Konstruktøren er offentlig etter pakkeflyttingen i #204. |
| `EI_EXPOSE_REP2` | Konstruktøren til [`AuthenticationService`](../../bithub/springboot/restserver/src/main/java/rest/auth/AuthenticationService.java) | Tokenregisteret må deles, slik at utlogging gjelder alle endepunkter (#176). |
| `EI_EXPOSE_REP2` | Konstruktøren til [`UserController`](../../bithub/springboot/restserver/src/main/java/rest/api/UserController.java) | Spring deler [`TokenService`](../../bithub/springboot/restserver/src/main/java/rest/auth/TokenService.java), slik at utlogging gjelder alle kall (#176). |

## Maven og automatisert kvalitetssikring

Maven er vårt felles verktøy for bygging, testing og kontroll av kodekvalitet. Da kan de samme kontrollene kjøres uansett hvilken IDE eller plattform et gruppemedlem bruker.

Den samlede kvalitetssjekken kjøres fra `bithub/` med:

```
mvn clean verify
```

Kommandoen bygger alle fire modulene, kjører testene og gjennomfører kontrollene for kodekvalitet. JaCoCo lager rapport om testdekning under testkjøringen, og Checkstyle og SpotBugs kjører sine kontroller i `verify`-fasen. Ved å samle kontrollene i Maven reduserer vi risikoen for at gruppemedlemmer sjekker prosjektet på forskjellige måter.

## Kvalitetskontroll før sammenslåing

Før en pull request ferdigstilles, henter vi siste versjon av `master` inn i branchen. Eventuelle konflikter løses før prosjektet sjekkes på nytt.

Deretter kjører vi `mvn clean verify` for å sjekke at endringene virker sammen med den nyeste versjonen av prosjektet. Bygget skal fullføres uten feil, nye og eksisterende tester skal bestå, SpotBugs skal ikke rapportere feil, og endringen skal ikke føre til nye Checkstyle-warnings.

Relevant dokumentasjon skal også oppdateres hvis endringen påvirker arkitektur, funksjonalitet eller arbeidsflyt. For REST-API-et betyr det blant annet at statuskolonnen i [rest-api.md](rest-api.md) oppdateres når et endepunkt blir ferdig.

## Håndtering av advarsler og unntak

Advarsler og feil fra testene eller kvalitetsverktøyene skal som hovedregel undersøkes og rettes, ikke ignoreres.

Hvis det er nødvendig å legge inn et unntak i et kvalitetsverktøy, vurderer gruppen først hvorfor advarselen oppstår, og om koden kan skrives på en bedre måte. Unntak skal være så avgrensede som mulig, og bare brukes når vi kan begrunne valget. Alle unntakene vi har i dag, står i avsnittet om SpotBugs over.

Slik fortsetter Checkstyle, SpotBugs og de andre kontrollene å fungere som reelle kvalitetssjekker, og ikke bare som krav vi omgår for å få bygget til å bestå.
