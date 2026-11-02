# Bruk av KI i release 3

## Verktøy og bruksområder

I arbeidet med release 3 har vi brukt de samme verktøyene som tidligere:
ChatGPT, Google Gemini, Claude og OpenAI Codex. Siden denne releasen har
handlet mye om å dele appen i en klient og en REST-server, har KI i større
grad blitt brukt til å forstå disse nye konseptene. Ellers har bruken av KI
vært omtrent som i release 1 og 2. Gruppemedlemmene har brukt KI til mye av
det samme, fordi releasen ble delt inn i tre sprinter der alle har jobbet med
ulike deler av prosjektet, som koding, tester, UI og dokumentasjon. KI har
fungert som en god veileder som ga oss et utgangspunkt, mens gruppen selv har
tatt beslutningene om hvordan prosjektet skulle utvikles videre.

## Klient, server og REST-API

Release 3 var første gang gruppen jobbet med en REST-server, og her har KI vært
svært nyttig til å forklare underveis. Forklaringene fra KI ga oss et utgangspunkt,
men vi sjekket dem alltid opp mot emnets fagmateriale, for eksempel forelesningene
om REST-API.

## Feilsøking og tester

Som tidligere har KI vært nyttig i feilsøkingsprosessen, særlig for å identifisere
og løse problemer i koden. Med KI har det blitt både mer oversiktlig og mer effektivt
å finne eventuelle feil eller svakheter i koden. Den har i tillegg vært til stor hjelp
når vi har skrevet tester til appen, da KI raskt kan tolke feilmeldinger og peke ut
områder der det mangler kode, eller der koden må endres.

## Kode og kodegjennomgang

I release 3 har vi gjort flere refaktoreringer, blant annet å hente emnelisten fra
lagringen i stedet for fra UI-et og  å sikre JSON-lagringen. Her har KI vært et godt
hjelpemiddel til å få oversikt over hvordan koden fungerte fra før av og hvordan den
skal endres slik at den passer med den nye strukturen i prosjektet.

## Dokumentasjon

Vi har brukt KI som språkkontroll for å finne skrivefeil og forbedre flyten i
dokumentasjonen i release 3. For at KI ikke skulle ta over teksten, ba vi den
om en liste over foreslåtte endringer i stedet for en ferdig skrevet tekst. På
denne måten kunne vi vurdere hver endring selv og avgjøre om den skulle tas med.
For eksempel påpekte KI at en oppdatering av ai-tools.md fra release 2 ville bli
for lik den forrige, og foreslo å beholde strukturen, men skrive innholdet på nytt
med fokus på release 3.

## Erfaringer, kritisk vurdering og læring

Erfaringene vi fikk fra release 2, har påvirket hvordan vi jobbet med KI i release 3.
Vi visste at KI ofte mangler kontekst om kodebasen, og har derfor bare gitt den filene
som er relevante for feilmeldingen vi trenger hjelp med. Dette gjør vi for at den bare
skal hjelpe med den konkrete feilen, og ikke blande seg inn i deler av prosjektet vi ikke
trenger hjelp med.

På samme måte som i release 2 har vi observert at KI fortsatt kan gi informasjon som den
har gjettet seg fram til. Gjetningene kan bygge på kontekst den har fått fra deler av
filer eller terminalen når vi har spurt om hjelp med feilmeldinger. For eksempel kan den
gjette seg fram til hva som er gjort i prosjeketet bare ut fra branch navn, selv om det
ikke nødvendigvis stemmer. Dette viser hvor viktig det er å gå gjennom alt KI genererer,
og dobbeltsjekke det mot selve prosjektet.

Vi har også erfart at KI er nyttig på ulike måter avhengig av oppgaven. Når vi skulle
lære noe nytt, som REST-API, var KI god til å forklare konsepter. Ved feilsøking i kode
var den mest nyttig til å tolke feilmeldinger. I begge tilfeller har vi lært mest når vi
har brukt forklaringene til å skrive og teste koden selv, i stedet for å kopiere ferdige
løsninger. KI er flink til å følge retningslinjer så lenge vi gir tydelige beskjeder om
hva vi trenger hjelp med og hvordan den skal svare.