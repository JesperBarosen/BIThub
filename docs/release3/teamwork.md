# Teamrefleksjon om samarbeid og organisering

## Planlegging

Release 3 har vært den største releasen i prosjektet, og vi delte den
derfor inn i tre sprinter. Før vi begynte, lagde vi en `issues_oversikt.md
med mål for releasen og en oversikt over hver sprint. Hver sprint hadde
en tidsfrist på ca. to uker. Tidsfristene sikret at arbeidet ble fordelt
over flere uker i stedet for å samles mot slutten.

## Koordinering

I hver sprint ble issuene fordelt på tre par, og parene ble rullert for
hver sprint, slik at alle fikk jobbe med alle. Issuene ble satt opp i
rekkefølge, siden mange var avhengige av hverandre og måtte merges før
neste kunne startes. Alle issuene fikk en beskrivelse etter malen i `workflow.md`.

I tillegg fortsatte vi med ukentlige møter som vekslet mellom mandag og
tirsdag, og hadde to ekstra møter i leveringsuken. Her satte vi av tid til
å jobbe sammen, diskutere og avklare spørsmål om appen.

## Tilbakemelding fra release 2

I tilbakemeldingen på release 2 fikk vi beskjed om at flere commit-meldinger
var for korte og lite beskrivende, for eksempel «Legg til ...» og «Endre readme».
I release 3 har vi derfor blitt mer bevisste og fulgt malen i `workflow.md` for
alle commits, med en beskrivende oppsummering som tittel og en tekst som forklarer
hva som er endret og hvorfor. Dette gjør endringer lettere å følge, særlig når vi
reviewer hverandres pull requests.

## Erfaringer

Vi har også lært fra feilen med `Co-authored-by` fra release 2, slik at parprogrammeringen
nå blir registrert på GitHub. Vi hadde fortsatt noen merge-konflikter, men det skyldtes
i stor grad at mange issues var avhengige av hverandre. Selv med god oversikt over issuene
er vi seks personer som jobber parallelt, og da er det vanskelig å unngå at flere endrer
de samme filene samtidig.