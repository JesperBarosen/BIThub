# Asynkrone REST-kall i JavaFX

Dette dokumentet beskriver mønsteret innført i issue #173. Det skal gjenbrukes i #178, #179 og #181 når flere skjermer kobles til REST-API-et.

## Ansvarsfordeling

- **`BackgroundTaskRunner` (`ui`):** Kjører ett kall om gangen på en daemon-bakgrunnstråd. `runningProperty()` viser om et kall pågår. `onSuccess` og `onFailure` kjøres på JavaFX-tråden.
- **Kontrolleren (`ui`):** Leser alle skjemaverdier på JavaFX-tråden før kallet, viser lastetilstand, binder relevante knapper og felt til `runningProperty()`, og oppdaterer UI i tilbakemeldingene.
- **Tjeneste og repository:** Utfører operasjonen, herunder HTTP. Disse skal ikke lese eller skrive JavaFX-komponenter.
- **`RemoteReviewRepository`:** Har tilkoblingstidsavbrudd på 5 sekunder og tidsavbrudd per HTTP-forespørsel på 10 sekunder. Dette er HTTP-tidsavbrudd, ikke en tidsgrense som `BackgroundTaskRunner` setter.

## Mønster ved henting

1. Nullstill tidligere visning og vis en tekst som «Henter emner …» eller «Henter anmeldelser ...».
2. Start repository- eller tjenestekallet gjennom `taskRunner.execute(...)`.
3. Vis resultatet i `onSuccess`. Ved tomt resultat vises en egen, forståelig melding.
4. Vis en kort feilmelding i `onFailure`, uten å blokkere JavaFX-tråden.
5. Ved ny henting eller navigering bort kalles `taskRunner.cancel()` før skjermbyttet. Et sent svar får da ikke oppdatere den gamle skjermen.

Implementerte eksempler: `VelgFagController` og `LeseAnmeldelserController`.

## Mønster ved innsending og endring

1. Les og kopier skjemaverdiene til lokale variabler **før** bakgrunnskallet starter. Ikke les JavaFX-felter fra bakgrunnstråden.
2. Kontroller at det ikke allerede pågår et kall. Deaktiver sendeknappen og eventuelt redigerbare felt med binding til `taskRunner.runningProperty()`.
3. Vis «Sender ...» eller en tilsvarende lastemelding.
4. Utfør tjenestekallet via `taskRunner.execute(...)`.
5. **Bare ved bekreftet suksess:** Vis bekreftelse og tøm eller oppdater skjemaet.
6. **Ved feil:** Behold brukerens skjemainnhold, vis en forståelig feilmelding og gjør feltene tilgjengelige igjen. Skillet mellom gyldig valideringsfeil og nettverksfeil håndteres i kontrollerens feilkallback.
7. Kall `taskRunner.cancel()` når brukeren navigerer bort. Et avbrudd hindrer oppdatering av feil skjerm, men garanterer **ikke** at en HTTP-forespørsel eller lagring på serveren faktisk stoppes.

Implementert eksempel: `SkrivAnmeldelseController`. Ikke send automatisk på nytt etter usikker feil ved lagring: Serveren kan ha lagret forespørselen selv om svaret aldri nådde klienten.

## Trådregler

- `execute()` og `cancel()` skal kalles fra JavaFX-tråden.
- Operasjonen som sendes til `execute()` skal ikke håndtere JavaFX-noder.
- `onSuccess` og `onFailure` kjøres på JavaFX-tråden og kan endre UI.
- `runningProperty()` kan brukes til å binde skjemafelt og knapper; det hindrer gjentatte innsendinger mens et kall pågår.
- Navigasjonshandlinger skal fremdeles være tilgjengelige mens appen venter på et svar.

## Tester

Testene bruker falske repository-implementasjoner med kontrollert forsinkelse, slik at de ikke avhenger av at REST-serveren kjører:

- `BackgroundTaskRunnerTest`: Bakgrunnstråd, callback på JavaFX-tråden, avvisning av duplikatkall og ignorering av sent svar etter avbrudd.
- `VelgFagControllerTest`: Lastetilstand, utilgjengelig server og navigering med et utestående kall.
- `LeseAnmeldelserControllerTest`: Henting av anmeldelser, lastetilstand, feil og sent svar etter navigering.
- `SkrivAnmeldelseControllerTest`: Bekreftet innsending, dobbel innsending, bevaring av skjemainnhold ved feil og navigering under lagring.

Kjør alle tester og kvalitetskontroller fra `bithub/`:

```cmd
mvn clean verify
```

Dette mønsteret gjelder JavaFX-trådbruk og UI-tilstand. Detaljerte HTTP-kontrakter, token-/sesjonshåndtering og integrasjonstester hører til sine respektive issues (#176, #180 og videre arbeid med nye skjermer).
