# Utfordringer i prosjektet

## 1. Merge-konflikter på grunn av fordeling og avhengigheter

### Utfordringen

I release 1 jobbet alle seks hver for seg, ofte i de samme filene. Issuene var korte, og det sto sjelden hvilke filer de ville endre eller hvilke andre issues de var avhengige av. Flere grener endret derfor de samme klassene samtidig, særlig kontrollerne og FXML-filene i `ui`. Da den ene grenen ble merget, fikk den andre ofte mange konflikter. Noen ganger måtte vi gjøre om alt arbeidet i et issue fordi koden det bygde på, hadde endret seg. Dette var veldig tidkrevende.

### Tiltak

- Vi skrev tydeligere issues, med beskrivelse, motivasjon og konkrete oppgaver. Vi skrev også hvilke issues de avhenger av og hvilke filer de berører.
- Vi fordelte oppgavene før sprinten startet, slik at hvert par fikk en del av koden som hang sammen.
- Fra release 2 jobbet vi med parprogrammering gjennom resten av prosjektet. Vi roterte på parene, slik at flest mulig lærte mest mulig om koden. Det ga bedre samlet forståelse av prosjektet og færre konflikter.
- Vi fikk en tydelig workflow: issue, egen gren, PR med fast mal, review fra et annet par og deretter merge.
- Vi kommuniserte bedre underveis. Vi sa fra når en PR endret mye i delte klasser, avtalte hvilken PR som skulle merges først, og merget ofte `master` inn i egen gren for å unngå feil i `master`.

### Lærdom

Vi lærte at de fleste konfliktene oppsto før vi begynte å kode, i planleggingen. Hvis vi tenker gjennom avhengigheter og fordeler etter hvilke deler av koden som henger sammen, får vi færre konflikter og mindre dobbeltarbeid. Parprogrammering og en fast workflow gjorde samarbeidet mer forutsigbart, og jevnlig kommunikasjon gjør at konfliktene som likevel oppstår, blir små.

## 2. Overgangen fra lokal fil til REST-server

### Utfordringen

I release 3 gikk vi fra en app som lagret direkte i en JSON-fil, til en klient som snakker med en Spring Boot-server. `ui` var avhengig av `file`-modulen, testene brukte lagring på disk, og klient og server måtte være enige om endepunkter, JSON-format og statuskoder. Siden flere par jobbet på hver sin side samtidig, forventet klient og server ulike ting.

### Tiltak

- Vi skrev API-kontrakten (`rest-api.md`) med endepunkter og feilkoder før vi kodet, slik at klient og server kunne lages parallelt.
- Vi brukte grensesnittet `ReviewRepository` i `ui`. Da kunne `RemoteReviewRepository` byttes inn uten å endre kontrollerne.
- Vi fjernet avhengigheten fra `ui` til `file` i et eget issue.
- Vi delte overgangen i mange små issues med avhengigheter mellom seg.

### Lærdom

Når to deler av systemet utvikles parallelt, må grensesnittet mellom dem avtales og skrives ned først. En felles kontrakt og et tydelig grensesnitt i koden gjorde at vi kunne bytte lagringsløsning uten å skrive om resten av appen. Kontrakten må også oppdateres i samme PR som endrer et endepunkt, ellers kommer klient og server ut av takt. Store endringer blir lettere å styre når de deles i små steg som testes og merges hver for seg.
