# Arbeidsvaner

Dette dokumentet beskriver arbeidsvanene gruppen bruker i utviklingen
av BIThub. Arbeidsvanene skal gjøre det tydelig hvem som arbeider med
hva, sikre samarbeid mellom gruppemedlemmene og bidra til jevn
kvalitet gjennom prosjektet.

## Issues som utgangspunkt for arbeid

Arbeidet i prosjektet tar utgangspunkt i issues. Et issue beskriver
hva som skal gjøres, hvorfor oppgaven er nødvendig og hvilke kriterier
som må være oppfylt før oppgaven regnes som ferdig.

Issues brukes for å gjøre arbeidsfordelingen tydelig og for å kunne
spore endringer i kode og dokumentasjon tilbake til konkrete oppgaver.
Issues som tilhører en innlevering knyttes til den aktuelle milepælen.

Den konkrete arbeidsflyten for issues, brancher, commits og Pull
Requests er beskrevet i `workflow.md`.

## Parprogrammering

Arbeidet i release 2 gjennomføres i par. Under programmering fungerer
en person som driver og skriver koden, mens den andre fungerer som
navigator og følger med på løsningen, vurderer valg og foreslår
forbedringer.

Rollene byttes underveis slik at begge deltar aktivt i utviklingen.
Dette bidrar til at flere gruppemedlemmer får kjennskap til ulike deler
av prosjektet, samtidig som løsninger vurderes av mer enn en person
mens de utvikles.

Arbeid som utføres i par dokumenteres i commit-historikken med
`Co-authored-by`.

## Fordeling og koordinering av oppgaver

Oppgaver fordeles mellom gruppemedlemmene gjennom issues. Hvert issue
tildeles de personene som skal arbeide med oppgaven, slik at det er
tydelig hvem som har ansvar for hva.

Når flere oppgaver avhenger av hverandre, koordineres arbeidet mellom
parene før implementeringen starter. Dette er spesielt viktig i release
2, hvor domenelag, persistenslag og brukergrensesnitt skal utvikles slik
at de fungerer sammen.

Gruppen forsøker å unngå at en del av prosjektet utvikles langt foran
andre deler som den er avhengig av.

## Små og avgrensede endringer

Arbeidet deles så langt det er hensiktsmessig opp i små og avgrensede
endringer. Hver branch er knyttet til et issue, og commits skal
representere tydelige og meningsfulle steg i arbeidet.

Dette gjør commit-historikken lettere å forstå og gjør det enklere å
gjennomgå endringer i en Pull Request. Det reduserer også risikoen for
at flere urelaterte endringer blir blandet sammen i samme commit.

## Kodegjennomgang

Endringer integreres i `master` gjennom Pull Requests. Før en Pull
Request merges, skal endringene gjennomgås av andre gruppemedlemmer.

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