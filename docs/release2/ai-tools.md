# Bruk av KI i release 2

## Verktøy og bruksområder

I arbeidet med release 2 har vi brukt ChatGPT, Google Gemini, Claude og Codex.
Bruken har variert mellom gruppemedlemmene. Vi har blant annet fått hjelp til å
forstå oppgaveteksten, forklare kode og undersøke feilmeldinger. I noen oppgaver
har KI også foreslått og skrevet kodeendringer. Forslagene har vært utgangspunkt
for videre vurdering og testing i prosjektet.

## Feilsøking og tester

Vi har brukt KI til å tolke feilmeldinger og advarsler fra Maven, Checkstyle
og SpotBugs i terminalen. Vi har også fått forslag til årsaker til testfeil
og til tester som kunne undersøke bestemte feilsituasjoner. Et eksempel var filtester
som feilet på Mac fordi de forventet at innhenting av filstørrelsen for en mappe
skulle gi et unntak. KI hjalp oss å forstå hvorfor dette ikke skjedde, og
forklarte hvordan Mockito kunne brukes til å simulere unntaket i stedet.
Testen ble derfor endret slik at unntaket ble simulert eksplisitt med Mockito.

Forslagene ble kontrollert ved å kjøre relevante tester underveis og
`mvn test` for å sjekke testene samlet. Med `mvn verify` kontrollerte vi også at 
prosjektets kvalitetskontroller, blant annet Checkstyle og SpotBugs, passerte.

 
## Kode og kodegjennomgang 

KI ble også brukt som støtte under kodegjennomgang og refaktorering. Et eksempel 
var arbeidet med Checkstyle og JavaDoc, der KI hjalp oss med å tolke advarsler og 
foreslå endringer som fulgte prosjektets kodestandard. Vi brukte også KI til å 
vurdere avhengigheter mellom modulene og se etter steder der ansvaret mellom 
`ui`, `file` og `core` kunne være tydeligere. Forslagene ble vurdert opp mot 
den eksisterende arkitekturen og koden i prosjektet før vi gjorde endringer. 
Etterpå brukte vi tester og kvalitetsverktøy for å kontrollere at endringene ikke 
introduserte nye feil.

## Dokumentasjon 

Vi oppdaterte filstier og lenker underveis i prosjektet etter hvert som
strukturen endret seg. Mot slutten brukte vi KI til å sammenligne
dokumentasjonen med koden for å fange opp feil vi kunne ha oversett.
Et eksempel var en død lenke til README-filen i `ui`-mappen, som skulle
peke til `bithub/README.md`. 
KI-gjennomgangen avdekket også at README-en nevnte en kontroller som ikke 
lenger fantes. Vi sjekket hvert punkt mot koden før vi rettet det.
På samme måte brukte vi KI til å kontrollere at klassediagrammene og
pakkediagrammet i `arkitektur/` stemte med koden.

## Erfaringer, kritisk vurdering og læring

KI har vært nyttig som støtte gjennom release 2, men forslagene har ikke 
alltid vært riktige eller relevante for prosjektet. Vi erfarte at KI ofte 
manglet nødvendig kontekst om kodebasen og derfor kunne foreslå endringer 
som virket fornuftige isolert, men som ikke passet med eksisterende kode 
eller arkitektur. Dette gjorde det viktig å kontrollere forslagene mot 
prosjektet før de ble brukt.

Et eksempel var når KI foreslo endringer som gikk utenfor omfanget til 
issuet vi jobbet med, selv om løsningen kunne vært relevant for prosjektet 
generelt. Slike forslag ble derfor vurdert opp mot issuets acceptance 
criteria før vi gjorde endringer.

Vi erfarte også at kvaliteten på svarene ble bedre når KI fikk konkrete 
feilmeldinger, relevante kodefiler og informasjon om hvordan prosjektet 
var bygget opp. KI fungerte spesielt godt til å forklare feil, foreslå 
mulige årsaker og peke på områder som burde undersøkes nærmere. Samtidig 
måtte kodeendringer og andre forslag fortsatt vurderes av gruppen og 
kontrolleres gjennom tester og kvalitetsverktøy.

Gjennom arbeidet har vi derfor blitt mer bevisste på å bruke KI som et 
støtteverktøy fremfor som en fasit. Det har vært mest nyttig når 
forslagene har blitt brukt som utgangspunkt for videre undersøkelse, 
testing og diskusjon i gruppen.