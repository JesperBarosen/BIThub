# Bruk av KI i release 3

## Verktøy

- Claude og ChatGPT til utkast til kode og tekst, feilsøking av bygget, ekstra kontroll ved review og dokumentasjon.
- Google Gemini og OpenAI Codex i mindre grad, for å sammenligne svar.

Gruppen bestemte hva som skulle lages, og alt fra KI ble gått gjennom og testet før det ble tatt med.

## Hva vi brukte KI til

- **Forstå REST.** REST var nytt for oss. KI forklarte konseptene, og vi sjekket forklaringene mot forelesningen «OO Principles + REST APIs».
- **Utkast.** For noen issues lagde KI utkast til kode og tekst. For disse gikk vi gjennom utkastene linje for linje, tilpasset dem prosjektet og testet dem før de ble reviewet av et annet par. Review fant også ting KI ikke hadde tenkt på. I reviewen av [PR #245](https://git.ntnu.no/IT1901-2026-groups/gr2610/pull/245) så Tias at samtidige endringer av navn og passord kunne overskrive hverandre.
- **Feilsøking.** KI hjalp oss å tolke feilmeldinger, for eksempel Checkstyle-regelen `VariableDeclarationUsageDistance`.
- **Review og dokumentasjon.** Etter at vi hadde gått gjennom pull requestene, brukte vi KI som en ekstra kontroll. KI hjalp også med formuleringer i PR-beskrivelser og commit-meldinger, og med å finne steder der dokumentasjonen ikke stemte med koden, for eksempel at README nevnte «brukernavn».

## Merking av KI-kode

Kode som bygger på utkast fra KI, er merket med samme kommentar overalt:

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.

- Står kommentaren over en hel klasse eller metode, gjelder den hele klassen eller metoden. Da viser klassen eller metoden selv hvor koden slutter.
- Står KI-koden inne i en metode, markerer vi også slutten med `// Slutt på kode laget med hjelp av KI.`
- Kommentaren står over Javadoc-en, slik at den er det første man ser.

Søk etter «Laget med hjelp av KI» for å finne all KI-kode. Hvordan merkingen ble lagt til, står i [#252](https://git.ntnu.no/IT1901-2026-groups/gr2610/issues/252).

## Erfaringer

KI gjetter når den mangler kontekst, og den tok feil, for eksempel om hvilken JavaFX-versjon ControlsFX er bygget for, og med commit-meldinger som ikke fulgte malen vår. Vi fanget det opp fordi vi sjekket mot prosjektet. Utkast sparte tid, men bare når vi forsto dem godt nok til å forklare dem i review. Vi lærte mest når vi brukte KI til å forstå og kontrollere, ikke når vi tok imot ferdige svar.

I release 1 og 2 brukte vi nesten ikke KI til å generere kode, så det var lite å merke utenom regex-ene i `User.java`. Da vi begynte å la KI foreslå kode i release 3, endret vi ikke rutinene for merking. Det oppdaget vi under sluttsjekken, og merket koden i [#252](https://git.ntnu.no/IT1901-2026-groups/gr2610/issues/252). Til neste gang merker vi koden i samme pull request som den blir lagt til.
