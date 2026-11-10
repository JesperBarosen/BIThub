# Arbeidsvaner

Dette dokumentet beskriver arbeidsvanene gruppen bruker i utviklingen
av BIThub. Arbeidsvanene fra release 2 gjelder fortsatt, men i release 3
har vi lagt til sprinter i prosjekttavlen på GitHub og tydeligere regler
for avhengigheter og kodegjennomgang. Arbeidsvanene skal gjøre det tydelig
hvem som arbeider med hva, sikre samarbeid mellom gruppemedlemmene og bidra
til jevn kvalitet gjennom prosjektet.

Den konkrete arbeidsflyten for issues, brancher, commits og pull requests,
med maler, er beskrevet i [workflow.md](../release3/workflow.md).

## Sprintplanlegging

Release 3 er delt inn i tre sprinter med egne tidsfrister. I starten av
hver sprint går hele gruppen sammen gjennom issuene for sprinten og blir
enige om innhold, rekkefølge og fordeling, før issuene opprettes på GitHub.
På den måten er alle enige om hva som skal gjøres, og avhengigheter mellom
issuene blir oppdaget før noen begynner å kode.

For hver sprint er det satt opp hvilke issues som hører til og i hvilken
rekkefølge de bør gjøres, slik at fremdriften kan følges underveis og ikke
bare mot innleveringen. Endringer som berører mange filer, som å samle
klassene i pakker (#204), legges først i sprinten med prioritet «Høy», slik
at de andre parene bygger videre på den nye strukturen.

## Issues som utgangspunkt for arbeid

Alt arbeid tar utgangspunkt i et issue. Et issue beskriver hva som skal
gjøres, hvorfor oppgaven er nødvendig og hvilke kriterier som må være
oppfylt før oppgaven regnes som ferdig. Issues brukes for å gjøre
arbeidsfordelingen tydelig og for å kunne spore endringer i kode og
dokumentasjon tilbake til konkrete oppgaver.

Under «Tilleggsinformasjon» står det hvilke issues oppgaven avhenger av,
for eksempel «Avhenger av #179 (profilsiden)». Et issue startes først når
issuene det avhenger av, er merget. Står det at to issues endrer de samme
filene, avtaler parene hvem som merger først.

## Prosjekttavle

Alle issues for release 3 ligger på prosjekttavla til gruppen på GitHub.
Tavla har kolonnene Backlog, Ready, In Progress, In review og Done, og hvert
issue har feltene Sprint og Priority. Et issue flyttes til In Progress når
paret begynner på det, til In review når pull requesten er åpnet, og til Done
når den er merget. Slik kan alle se hva som gjenstår i sprinten, hva som
venter på review, og hva som haster.

Issues knyttes i tillegg til milepælen for innleveringen (Release3), slik at
vi har en samlet oversikt over alt som må være ferdig før fristen.

## Parprogrammering

Nesten alle arbeidsoppgaver gjennomføres i par, og parene bytter på å kode.
Små oppgaver, som å gi modulen nytt navn eller fjerne `.DS_Store`, ble noen
ganger gjort av én person. Arbeid som utføres i par, dokumenteres i
commit-historikken med `Co-authored-by`, slik det er beskrevet i
[workflow.md](../release3/workflow.md).

I release 3 er gruppen delt inn i tre par, A, B og C, og hvert par har ansvar
for ett område i hver sprint, for eksempel REST-API, innlogging, rydding eller
klient. Ved hver ny sprint blir én person i hvert par værende, mens den andre
bytter til et nytt par. Den som blir værende, kjenner koden og oppgavene fra
forrige sprint og kan sette den nye inn i dem, så arbeidet ikke må starte på
nytt. Ingen par er like i to sprinter, så alle får jobbe med flere i gruppen
og med nye deler av koden.

Blir et par ferdig med sine issues før de andre, hjelper en av dem gjerne et
annet par med deres issues. Derfor har noen issues tre personer tildelt, og
commit-meldingene skal da ha `Co-authored-by` for begge de andre. Oppgaver
som angår alle, som dokumentasjonen av REST-API-et og sluttsjekken, har hele
gruppen tildelt.

## Små og avgrensede endringer

Arbeidet deles så langt det er hensiktsmessig opp i små og avgrensede
endringer. Hver branch er knyttet til ett issue, og hver commit skal være
et tydelig steg som bygger og består testene for seg.

Dette gjør commit-historikken lettere å forstå og gjør det enklere å
gjennomgå endringer i en pull request. Det reduserer også risikoen for
at flere urelaterte endringer blir blandet sammen i samme commit.

## Kodegjennomgang

Endringer integreres i `master` gjennom pull requests, med malen fra
[workflow.md](../release3/workflow.md). Malen krever at det står hva som er
endret, hvordan endringen kan testes, og at sjekklisten før review er fylt ut.

En pull request gjennomgås av minst to andre på gruppen, og minst én av dem er
fra et annet par enn det som har gjort arbeidet. Den kan først merges når begge
har godkjent. Finner den som reviewer,
noe som må rettes, brukes «Request changes», gjerne med kodeforslag direkte
på linjene. Paret retter endringene, svarer på kommentarene og ber om ny
review. I #245 (navnebytte) fant reviewen for eksempel at samtidige endringer
av navn og passord kunne overskrive hverandre, og løsningen ble rettet før
den ble merget.

Kodegjennomgangen brukes til å kontrollere at løsningen oppfyller kravene i
issuet, passer sammen med resten av prosjektet og ikke introduserer
unødvendige eller problematiske endringer. Har `master` endret seg siden
branchen ble laget, hentes siste versjon inn i branchen før sammenslåing, og
`mvn clean verify` kjøres på nytt, slik at tester, Checkstyle og SpotBugs er
grønne sammen med de nye endringene. Er `master` uendret, er branchen allerede
oppdatert, og da trengs ikke dette steget.
