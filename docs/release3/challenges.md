# Utfordringer i prosjektet

## 1. Merge-konflikter på grunn av fordeling og avhengigheter

### Utfordringen

I release 1 jobbet alle seks hver for seg, ofte i de samme filene. Issuene var korte, og det sto sjelden hvilke filer de ville endre eller hvilke issues de var avhengige av. Flere grener endret derfor de samme klassene samtidig, særlig kontrollerne og FXML-filene i `ui`. Da den ene grenen ble merget, fikk den andre ofte mange konflikter, og noen ganger måtte vi gjøre om arbeidet fordi koden det bygde på, hadde endret seg.

### Tiltak

- Vi skrev tydeligere issues med beskrivelse, motivasjon og konkrete oppgaver, og med hvilke issues og filer de berører.
- Vi fordelte oppgavene før sprinten, slik at hvert par fikk en del av koden som hang sammen.
- Fra release 2 brukte vi parprogrammering og roterte på parene, slik at flest mulig lærte mest mulig av koden.
- Vi fikk en fast workflow: issue, egen gren, PR med fast mal, review fra et annet par og deretter merge.
- Vi sa fra når en PR endret mye i delte klasser, avtalte rekkefølgen på merging, og merget ofte `master` inn i egen gren, slik at konflikter ble løst før PR-en ble merget.

### Lærdom

De fleste konfliktene oppsto i planleggingen, før vi begynte å kode. Når vi tenker gjennom avhengigheter og fordeler etter hvilke deler av koden som henger sammen, får vi færre konflikter og mindre dobbeltarbeid. Parprogrammering og en fast workflow gjorde samarbeidet mer forutsigbart, og jevnlig kommunikasjon gjør at konfliktene som likevel oppstår, blir små.

## 2. Overgangen fra lokal fil til REST-server

### Utfordringen

I release 3 gikk vi fra en app som lagret direkte i en JSON-fil, til en klient som snakker med en Spring Boot-server. `ui` var avhengig av `file`, testene brukte lagring på disk, og klient og server måtte være enige om endepunkter, JSON-format og statuskoder. Siden flere par jobbet på hver sin side samtidig, forventet klient og server ulike ting.

### Tiltak

- Vi skrev API-kontrakten (`rest-api.md`) med endepunkter og feilkoder før vi kodet, slik at klient og server kunne lages parallelt.
- Grensesnittet `ReviewRepository` i `core` gjorde at `RemoteReviewRepository` i `ui` kunne erstatte lagringen i `file` uten at `ReviewService` eller kontrollerne måtte endres.
- Vi fjernet avhengigheten fra `ui` til `file` i et eget issue.
- Vi delte overgangen i mange små issues med avhengigheter mellom seg.

### Lærdom

Når to deler av systemet utvikles parallelt, må grensesnittet mellom dem avtales og skrives ned først. En felles kontrakt og et tydelig grensesnitt i koden gjorde at vi kunne bytte lagringsløsning uten å skrive om resten av appen. Kontrakten må oppdateres i samme PR som endrer et endepunkt, ellers kommer klient og server ut av takt. Store endringer blir lettere å styre når de deles i små steg som testes og merges hver for seg.