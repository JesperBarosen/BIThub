# Asynkrone REST-kall i JavaFX

Dette dokumentet beskriver mønsteret innført i issue #173. Alle skjermene som kaller REST-API-et, bruker det.

## Ansvarsfordeling

- **[`BackgroundTaskRunner`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java) (`ui`):** Kjører ett kall om gangen på en daemon-bakgrunnstråd. [`runningProperty()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L28) viser om et kall pågår. `onSuccess` og `onFailure` kjøres på JavaFX-tråden.
- **Kontrolleren (`ui`):** Leser alle skjemaverdier på JavaFX-tråden før kallet, viser lastetilstand, binder relevante knapper og felt til [`runningProperty()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L28), og oppdaterer UI i tilbakemeldingene.
- **Tjeneste og repository:** Utfører operasjonen, herunder HTTP. Disse skal ikke lese eller skrive JavaFX-komponenter.
- **[`RemoteReviewRepository`](../../bithub/ui/src/main/java/ui/client/RemoteReviewRepository.java):** Har tilkoblingstidsavbrudd på 5 sekunder og tidsavbrudd per HTTP-forespørsel på 10 sekunder. Dette er HTTP-tidsavbrudd, ikke en tidsgrense som [`BackgroundTaskRunner`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java) setter.

## Mønster ved henting

1. Nullstill tidligere visning og vis en tekst som «Henter emner …» eller «Henter anmeldelser ...».
2. Start repository- eller tjenestekallet gjennom [`taskRunner.execute(...)`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L45).
3. Vis resultatet i `onSuccess`. Ved tomt resultat vises en egen, forståelig melding.
4. Vis en kort feilmelding i `onFailure`, uten å blokkere JavaFX-tråden.
5. Ved ny henting eller navigering bort kalles [`taskRunner.cancel()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L90) før skjermbyttet. Et sent svar får da ikke oppdatere den gamle skjermen.

Implementert i [`CourseSelectionController`](../../bithub/ui/src/main/java/ui/controller/CourseSelectionController.java), [`ReadReviewsController`](../../bithub/ui/src/main/java/ui/controller/ReadReviewsController.java) og [`MyReviewsController`](../../bithub/ui/src/main/java/ui/controller/MyReviewsController.java).

### Emnesøk og innlasting

[`CourseSelectionController`](../../bithub/ui/src/main/java/ui/controller/CourseSelectionController.java) filtrerer innlastede emner lokalt med en
`FilteredList`, uten nye REST-kall når søket endres. Søkefeltet er
deaktivert mens emnene hentes. Ved ny innlasting beholdes søket og
brukes på de nye emnene, mens tidligere emnevalg fjernes.

Når søket endres, beholdes valgt emne bare hvis det fortsatt finnes
blant treffene. Ellers fjernes valget og «Gå videre» deaktiveres.
Statusmeldingen skiller mellom tom emnelagring og søk uten treff.
Endringer i søket overskriver ikke laste- eller feilmeldinger.

## Mønster ved innsending og endring

1. Les og kopier skjemaverdiene til lokale variabler **før** bakgrunnskallet starter. Ikke les JavaFX-felter fra bakgrunnstråden.
2. Kontroller at det ikke allerede pågår et kall. Deaktiver sendeknappen og eventuelt redigerbare felt med binding til [`taskRunner.runningProperty()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L28).
3. Vis «Sender ...» eller en tilsvarende lastemelding.
4. Utfør tjenestekallet via [`taskRunner.execute(...)`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L45).
5. **Bare ved bekreftet suksess:** Vis bekreftelse og tøm eller oppdater skjemaet.
6. **Ved feil:** Behold brukerens skjemainnhold, vis en forståelig feilmelding og gjør feltene tilgjengelige igjen. Skillet mellom gyldig valideringsfeil og nettverksfeil håndteres i kontrollerens feilkallback.
7. Kall [`taskRunner.cancel()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L90) når brukeren navigerer bort. Et avbrudd hindrer oppdatering av feil skjerm, men garanterer **ikke** at en HTTP-forespørsel eller lagring på serveren faktisk stoppes.

Implementert i [`WriteReviewController`](../../bithub/ui/src/main/java/ui/controller/WriteReviewController.java), [`EditReviewController`](../../bithub/ui/src/main/java/ui/controller/EditReviewController.java), [`LoginController`](../../bithub/ui/src/main/java/ui/controller/LoginController.java), [`RegisterUserController`](../../bithub/ui/src/main/java/ui/controller/RegisterUserController.java), [`ChangePasswordController`](../../bithub/ui/src/main/java/ui/controller/ChangePasswordController.java) og [`ProfileController`](../../bithub/ui/src/main/java/ui/controller/ProfileController.java). Ikke send automatisk på nytt etter usikker feil ved lagring: Serveren kan ha lagret forespørselen selv om svaret aldri nådde klienten.

## Trådregler

- [`execute()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L45) og [`cancel()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L90) skal kalles fra JavaFX-tråden.
- Operasjonen som sendes til [`execute()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L45) skal ikke håndtere JavaFX-noder.
- `onSuccess` og `onFailure` kjøres på JavaFX-tråden og kan endre UI.
- [`runningProperty()`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java#L28) kan brukes til å binde skjemafelt og knapper; det hindrer gjentatte innsendinger mens et kall pågår.
- Navigasjonshandlinger skal fremdeles være tilgjengelige mens appen venter på et svar.

## Tester

Testene bruker falske repository-implementasjoner med kontrollert forsinkelse, slik at de ikke avhenger av at REST-serveren kjører:

- [`BackgroundTaskRunnerTest`](../../bithub/ui/src/test/java/ui/util/BackgroundTaskRunnerTest.java): Bakgrunnstråd, callback på JavaFX-tråden, avvisning av duplikatkall og ignorering av sent svar etter avbrudd.
- [`CourseSelectionControllerTest`](../../bithub/ui/src/test/java/ui/controller/CourseSelectionControllerTest.java): Filtrering på emnekode og navn, tomt søk,  søk uten treff, bevaring og fjerning av emnevalg, statusmeldinger,
  søk ved ny innlasting, utilgjengelig server og navigering med et utestående kall. Søket endres på JavaFX-tråden uten å styre mus eller tastatur.
- [`ReadReviewsControllerTest`](../../bithub/ui/src/test/java/ui/controller/ReadReviewsControllerTest.java): Henting av anmeldelser, lastetilstand, feil og sent svar etter navigering.
- [`WriteReviewControllerTest`](../../bithub/ui/src/test/java/ui/controller/WriteReviewControllerTest.java): Bekreftet innsending, dobbel innsending, bevaring av skjemainnhold ved feil og navigering under lagring.

Kjør alle tester og kvalitetskontroller fra `bithub/`:

```cmd
mvn clean verify
```

Dette mønsteret gjelder JavaFX-trådbruk og UI-tilstand. Detaljerte HTTP-kontrakter, token-/sesjonshåndtering og integrasjonstester hører til sine respektive issues (#176, #180 og videre arbeid med nye skjermer).