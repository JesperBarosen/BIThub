# Bærekraft i BIThub

Vi har vurdert bærekraften i BIThub med Sustainability Awareness Framework (SusAF), som ser på fem dimensjoner: individuell, sosial, miljømessig, økonomisk og teknisk. Hver virkning er umiddelbar (kommer direkte av løsningen), muliggjørende (kommer av at den brukes over tid) eller strukturell (endrer samfunnet rundt). Vi har tatt utgangspunkt i at BIThub brukes av NTNU-studenter og kjører på én server.

## SusAD-diagram

<a href="bilder_release3/SusAD_diagram.png"><img src="bilder_release3/SusAD_diagram.png" alt="SusAD-diagram for BIThub" width="800"></a>

Diagrammet er laget i SusAD-malen fra faget ([susad.pptx](bilder_release3/susad.pptx)). Gule lapper er positive virkninger, røde er negative, og en pil betyr at én virkning påvirker en annen.

| ID | Virkning | Dimensjon | Nivå | Påvirker |
|---|---|---|---|---|
| 1 | Bedre grunnlag for å velge emner (+) | Individuell | Umiddelbar | 7 |
| 2 | Tryggere å være ærlig anonymt (+) | Individuell | Umiddelbar | 5 |
| 3 | Passord lagres som hash med salt (+) | Teknisk | Umiddelbar | 9 |
| 4 | Bare innloggede kan skrive (+) | Sosial | Muliggjørende | 5, 6 |
| 5 | Usaklige eller sårende anmeldelser (−) | Sosial | Muliggjørende | |
| 6 | Erfaringer deles mellom kull (+) | Sosial | Strukturell | 7 |
| 7 | Færre feilvalg og bedre emner (+) | Økonomisk | Strukturell | |
| 8 | Lite data og ingen bilder (+) | Miljø | Umiddelbar | |
| 9 | Serveren er alltid på, og hashingen er tung (−) | Miljø | Umiddelbar | |
| 10 | Moduler, tester og kodesjekk (+) | Teknisk | Muliggjørende | |
| 11 | JSON-filer skalerer dårlig (−) | Teknisk | Muliggjørende | |

## Individuell og sosial bærekraft

BIThub gir studentene et bedre grunnlag for å velge emner (1). Når erfaringer deles fra kull til kull (6), kan det over tid gi færre feilvalg og nyttige tilbakemeldinger til faglærerne (7).

Anonyme anmeldelser gjør det tryggere å si hva man mener (2), men kan også gi usaklige eller sårende kommentarer, og vi har ingen moderering (5). Derfor må man være logget inn for å skrive (4), og eieren lagres selv om navnet ikke vises.

Vi lagrer bare navn, e-post og anmeldelser, og passordene lagres som hash med salt (3). Brukerne kan slette egne anmeldelser, men ikke kontoen sin.

## Miljø

BIThub lagrer bare tekst i små JSON-filer og har ingen bilder eller video (8). Mest strøm bruker serveren, som må være på hele tiden, og passordhashingen med 600 000 iterasjoner (9). Hashingen er et bevisst valg for sikkerheten og skjer bare ved innlogging, registrering og passordbytte. Appen kjører på maskiner studentene allerede har.

## Økonomisk bærekraft

BIThub er bygget med gratis verktøy og biblioteker med åpen kildekode. Den største gevinsten er indirekte: færre feilvalg betyr mindre tid og studiefinansiering brukt på emner som ikke passer (7).

## Teknisk bærekraft

Koden er delt i modulene `core`, `file`, `ui` og `springboot/restserver`, og pakkene er delt etter ansvar ([pakkestruktur.md](pakkestruktur.md)). Over 600 tester, Checkstyle og SpotBugs holder koden ryddig ([code-quality.md](code-quality.md)) (10). Det gjør den lettere å forstå og endre for nye utviklere.

Svakheten er lagringen. JSON-filer fungerer for noen få brukere, men ikke for hele NTNU (11), som beskrevet i [persistence.md](persistence.md). Lagringen ligger bak `ReviewRepository` og `UserRepository`, så den kan byttes ut med en database uten å endre resten av appen.

## Hva kan forbedres

- Rapportering eller moderering av anmeldelser.
- Sletting av egen konto.
- Innlogging som går ut etter en tid.
- En database i stedet for JSON-filer.
- Testing av universell utforming, for eksempel med skjermleser.