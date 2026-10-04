
# Teamrefleksjon om samarbeid og organisering

Gjennom prosjektet har vi erfart hvordan planlegging og organisering påvirker både effektiviteten
og samarbeidet i gruppen. Ved starten av hver release delte vi arbeidet opp i issues på GitHub og
knyttet dem til en milepæl for innleveringen, slik at alle kunne se hva som gjensto. Hvert issue ble
tildelt minst to gruppemedlemmer, slik at parprogrammering ble en sentral del av arbeidet. Pull requests
ble gjennomgått av noen andre enn de som skrev koden, slik at flere fikk innsikt i endringene.

Vi har hatt et fast ukentlig møte tirsdager kl. 13:30 for å planlegge og jobbe sammen. Det har gjort
det enklere å avklare spørsmål som krever innspill fra hele gruppen. Samtidig har vi vært fleksible,
både med ekstra møter og med å fordele arbeidet på nytt når noen har trengt avlastning.

Samarbeidet var likevel ikke perfekt. Siden flere par jobbet i de samme modulene samtidig, fikk vi
en del merge-konflikter, særlig når en branch hadde levd lenge før den ble merget. Vi lærte at det
lønner seg å hente inn siste versjon av master ofte og å holde branchene små. Noen oppgaver ble forsinket
fordi de var avhengige av noe som ikke var ferdig, og vi ble etter hvert flinkere til å avklare slike
avhengigheter før vi begynte å kode. Mot slutten av release 2 oppdaget vi også at `Co-authored-by` sto
feil i commit-meldingene, slik at GitHub ikke registrerte parprogrammeringen. Feilen kom fra malen vi
alle fulgte, og viste oss at en felles mal også sprer feil til alle.

Den viktigste lærdommen er at felles regler gjør samarbeidet enklere. Etter at vi skrev `workflow.md`,
med faste maler for issues, branches, commits og pull requests, har arbeidet blitt mer oversiktlig,
og det er lettere å følge hverandres endringer.
