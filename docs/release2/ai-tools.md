# Bruk av KI i release 2

## Verktøy og bruksområder

I arbeidet med release 2 har vi brukt ChatGPT, Google Gemini, Claude og Codex.
Bruken har variert mellom gruppemedlemmene. Vi har blant annet fått hjelp til å
forstå oppgaveteksten, forklare kode og undersøke feilmeldinger. I noen oppgaver
har KI også foreslått og skrevet kodeendringer. Forslagene har vært utgangspunkt
for videre vurdering og testing i prosjektet.

## Feilsøking og tester

Vi har brukt KI til å tolke feilmeldinger i terminalen og få forklart hva som
kunne være årsaken, og hvordan feilene kunne rettes. Et eksempel var filtester
som feilet på Mac fordi de forventet at lesing av filstørrelsen til en mappe
skulle gi et unntak. KI hjalp oss å forstå hvorfor dette ikke skjedde, og
forklarte hvordan Mockito kunne brukes til å simulere unntaket i stedet.
Forklaringen gjorde det lettere å rette feilene. Etter
endringene kjørte vi testene på nytt for å kontrollere at de besto.
