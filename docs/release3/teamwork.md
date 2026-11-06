# Teamrefleksjon om samarbeid og organisering

## Planlegging

Release 3 har vært den største releasen i prosjektet, og vi delte den derfor
inn i tre sprinter. Før vi begynte, lagde vi et felles planleggingsdokument med
mål for releasen og en oversikt over hver sprint. I starten av hver sprint gikk
vi sammen gjennom issuene og ble enige om dem før de ble opprettet på GitHub.

## Koordinering

I hver sprint ble issuene fordelt på tre par. Ved ny sprint byttet vi par: én i hvert
par ble værende og satte den nye partneren inn i oppgavene. Ingen par var like i to
sprinter, så alle fikk jobbe med flere i gruppen og med nye deler av koden. Ble noen
ferdige tidlig, hjalp de et annet par. Noen oppgaver, som REST-dokumentasjonen og
contribution.md, gjorde hele gruppen sammen. Issuene ble satt opp i rekkefølge, siden
mange var avhengige av hverandre. Vi fortsatte med ukentlige møter, og hadde to
ekstra møter i leveringsuken.

## Tilbakemelding fra release 2

I tilbakemeldingen på release 2 fikk vi beskjed om at flere commit-meldinger
var for korte, for eksempel «Endre readme». I release 3 har vi fulgt malen i
[workflow.md](../release3/workflow.md), med en beskrivende tittel og en tekst
som forklarer hva som er endret og hvorfor. Vi rettet også formatet på
`Co-authored-by`, som var feil i malen i release 2, slik at GitHub nå
registrerer parprogrammeringen.

## Hva vi lærte

Rekkefølgen på issuene betyr like mye som fordelingen. Da store
omstruktureringer, som pakkeinndelingen, ble gjort først i sprinten, gikk
resten lettere. Når flere issues endret de samme filene samtidig, særlig
`Navigation`, fikk vi konflikter. Neste gang ville vi latt ett par eie slike
filer i en sprint. Review mellom parene fant også feil vi selv ikke så. Hvordan
vi løste merge-konfliktene, står i [challenges.md](challenges.md).
