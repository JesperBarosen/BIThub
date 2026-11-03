# Arbeidsvaner

Dette dokumentet beskriver arbeidsvanene gruppen bruker i utviklingen
av BIThub. Arbeidsvanene er videreført fra release 2 og oppdatert for
release 3. De skal gjøre det tydelig hvem som arbeider med
hva, sikre samarbeid mellom gruppemedlemmene og bidra til jevn kvalitet
gjennom prosjektet.

## Issues som utgangspunkt for arbeid

Arbeidet i prosjektet tar utgangspunkt i issues. Et issue beskriver
hva som skal gjøres, hvorfor oppgaven er nødvendig og hvilke kriterier
som må være oppfylt før oppgaven regnes som ferdig.

Issues brukes for å gjøre arbeidsfordelingen tydelig og for å kunne
spore endringer i kode og dokumentasjon tilbake til konkrete oppgaver.
Issues som tilhører en innlevering knyttes til den aktuelle milepælen.

Den konkrete arbeidsflyten for issues, brancher, commits og pull
requests er beskrevet i `workflow.md`.

## Parprogrammering

Arbeidsoppgaver i release 3 gjennomføres i par. Under parprogrammering fungerer
en person som driver og skriver koden, mens den andre fungerer som
navigator og følger med på løsningen, vurderer valg og foreslår
forbedringer.

Rollene byttes underveis slik at begge deltar aktivt i utviklingen.
Dette bidrar til at flere gruppemedlemmer får kjennskap til ulike deler
av prosjektet, samtidig som løsninger vurderes av mer enn én person
mens de utvikles.

Arbeid som utføres i par dokumenteres i commit-historikken med
`Co-authored-by` slik beskrevet i `workflow.md`.

I release 3 deles gruppen inn i tre par, A, B og C, som rulleres for
hver sprint. Parene settes opp slik at hvert gruppemedlem ikke er i
samme bokstavpar mer enn to ganger i løpet av releasen. Parprogrammeringen
planlegges slik at hvert gruppemedlem jobber i par med alle de andre i løpet
av prosjektet. Dette bidrar til at gruppemedlemmene får samarbeide med
hverandre og får bredere erfaring med prosjektet.

## Fordeling og koordinering av oppgaver

Oppgaver fordeles mellom gruppemedlemmene gjennom issues. Hvert issue
tildeles de personene som skal arbeide med oppgaven, slik at det er
tydelig hvem som har ansvar for hva.

Når flere oppgaver avhenger av hverandre, koordineres arbeidet mellom
parene før implementeringen starter. For hvert issue står det hvilke andre
issues det avhenger av, og et issue startes først når disse er merget.
Dette er spesielt viktig i release 3, hvor flere av parene jobber på tvers
av hverandre i de samme filene. For eksempel samordnes par B og par C når
innloggingsskjermen og gjestemodusen lages, og endringer som berører mange
filer, som å samle klassene i pakker, må gjøres først i sprinten før de andre
parene starter.

I release 3 fordeles issuene etter ansvarsområder, slik at hvert par har
ansvar for én del av prosjektet i hver sprint. Ansvarsområdene er for
eksempel REST-API, innlogging, rydding eller klient. Når parene byttes
til ny sprint, får gruppemedlemmene også nye arbeidsoppgaver.

## Små og avgrensede endringer

Arbeidet deles så langt det er hensiktsmessig opp i små og avgrensede
endringer. Hver branch er knyttet til et issue, og commits skal
representere tydelige og meningsfulle steg i arbeidet.

Dette gjør commit-historikken lettere å forstå og gjør det enklere å
gjennomgå endringer i en pull request. Det reduserer også risikoen for
at flere urelaterte endringer blir blandet sammen i samme commit.

## Kodegjennomgang

Endringer integreres i `master` gjennom pull requests. Før en pull
request merges, skal endringene gjennomgås av et annet par enn det som
har gjort arbeidet. Det er først etter at endringene er sett gjennom og
godkjent av et annet par, at pull requesten kan merges til `master`.
På denne måten blir endringene sett med nye øyne, og eventuelle forbedringer kan tas
opp før endringene tas inn i prosjektet.

Kodegjennomgangen brukes til å kontrollere at løsningen oppfyller
kravene i issuet, passer sammen med resten av prosjektet og ikke
introduserer unødvendige eller problematiske endringer.

Før sammenslåing hentes siste versjon av `master` inn i branchen, og
relevante tester og kodekvalitetskontroller kjøres på nytt.

## Milepæler og fremdrift

Issues knyttes til milepælen for den aktuelle innleveringen. Dette gir
gruppen en samlet oversikt over hvilke oppgaver som gjenstår og hvilke
som er ferdige.

Milepælene brukes sammen med issues for å følge fremdriften i
prosjektet og for å sikre at nødvendige oppgaver blir ferdigstilt før
innlevering.

Release 3 er delt inn i tre sprinter med egne tidsfrister.
For hver sprint er det satt opp hvilke issues som hører til og i hvilken
rekkefølge de bør gjøres, slik at fremdriften kan følges underveis og
ikke bare mot innleveringen.