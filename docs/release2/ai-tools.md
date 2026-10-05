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
som feilet på Mac fordi de forventet at lesing av filstørrelsen til en mappe
skulle gi et unntak. KI hjalp oss å forstå hvorfor dette ikke skjedde, og
forklarte hvordan Mockito kunne brukes til å simulere unntaket i stedet.
Forklaringen gjorde det lettere å rette feilene.

Forslagene ble kontrollert ved å kjøre relevante tester underveis og
`mvn test` for å sjekke testene samlet. Med `mvn verify` kontrollerte vi også
resultatene fra Checkstyle og SpotBugs. 

 
## Kode og kodegjennomgang 


## Dokumentasjon 

Vi oppdaterte filstier og lenker underveis i prosjektet etter hvert som
strukturen endret seg. Mot slutten brukte vi KI til å sammenligne
dokumentasjonen med koden for å fange opp feil vi kunne ha oversett.
Et eksempel var en død lenke til README-filen i `ui`-mappen, som skulle
peke til `bithub/README.md`.

## Erfaringer og kritisk vurdering 

 


## Hva vi lærte 

 

## Refleksjon 
