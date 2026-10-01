# Kodekvalitet

Dette dokumentet beskriver hvordan kodekvalitet sikres i BIThub.
Prosjektet bruker automatiserte tester og kodeanalyse gjennom Maven for
å oppdage feil og opprettholde en konsistent kodebase.

## Testing

Tester brukes for å kontrollere at eksisterende funksjonalitet fortsetter
å fungere når prosjektet endres. Domenelogikk testes med enhetstester,
mens brukergrensesnittet testes med relevante JavaFX-tester.

Alle modulene skal ha relevante tester. Persistenslaget testes blant
annet for lesing, skriving og håndtering av feiltilfeller.

Testene skal kunne kjøres gjennom Maven slik at kvalitetssikringen ikke
er avhengig av hvilken IDE et gruppemedlem bruker.

## Testdekning med JaCoCo

JaCoCo brukes for å måle hvilke deler av koden som blir kjørt av
testene. Rapporten brukes for å identifisere områder med mangelfull
testdekning, slik at det kan utvikles flere tester der det er nødvendig.

Prosjektet bruker JaCoCo Maven Plugin versjon 0.8.12. `prepare-agent`
kobler JaCoCo til testkjøringen, mens `report` kjøres i `test`-fasen og
genererer rapporten over testdekning.

Det er ikke konfigurert en fast minimumsgrense for testdekning. JaCoCo
brukes derfor som et analyseverktøy for å vurdere om de ulike delene av
prosjektet har rimelig testdekning.

Høy testdekning er ikke et mål i seg selv. Testene skal først og fremst
kontrollere forventet oppførsel, grenseverdier og relevante
feilsituasjoner. Når testene dekker dette for alle klassene, vil god
testdekning være en naturlig konsekvens av testarbeidet.

## Checkstyle

Checkstyle brukes for å kontrollere at Java-koden følger en konsistent
kodestil. Dette gjør koden lettere å lese og bidrar til at kode skrevet
av forskjellige gruppemedlemmer følger de samme reglene.

Checkstyle er integrert i Maven og bruker prosjektets regelsett i
`config/checkstyle/checkstyle.xml`. Regelsettet er basert på
`google_checks.xml`, med innrykk tilpasset til fire mellomrom.

Prosjektet bruker Maven Checkstyle Plugin versjon 3.6.0 med Checkstyle
versjon 10.26.1. Både produksjonskode og testkode kontrolleres, og
Checkstyle kjører målet `check` i `verify`-fasen.

VS Code bruker samme regelsett gjennom `java.checkstyle.configuration`,
satt til `${workspaceFolder}/config/checkstyle/checkstyle.xml`.
Repositoriets rotmappe må åpnes i VS Code for å bruke disse
prosjektinnstillingene.

Java-formatteringen styres av `.vscode/java-formatter.xml`.
Formatteren bruker fire mellomrom, mellomrom fremfor tabulatorer og
linjelengde på 100 tegn. Dette samkjører formatteringen med de valgte
Checkstyle-reglene. Automatisk formatering retter mange stilbrudd,
mens blant annet manglende JavaDoc må håndteres manuelt.

Checkstyle kan også kjøres separat fra `bithub/` med
`mvn checkstyle:check`.

Med dagens oppsett rapporteres regelbrudd på warning-nivå uten at
bygget nødvendigvis feiler. `BUILD SUCCESS` betyr derfor ikke at
koden er uten Checkstyle-warnings. Advarslene i rapporten må også
kontrolleres og rettes.

Eventuelle endringer i Checkstyle-konfigurasjonen skal gjøres bevisst
og begrunnes dersom de valgte reglene ikke passer prosjektet.

## SpotBugs

SpotBugs brukes til statisk analyse av Java-koden. Verktøyet analyserer
koden for å finne potensielle feil og problematiske kodemønstre som ikke
nødvendigvis blir oppdaget av kompilatoren eller testene.

Prosjektet bruker SpotBugs Maven Plugin versjon 4.8.6.6. Målet `check`
kjøres i `verify`-fasen og inngår dermed i den samlede
kvalitetssjekken.

SpotBugs er konfigurert til å generere HTML-rapport, mens XML-output er
deaktivert. Prosjektet bruker `config/spotbugs/exclude.xml` som filter
for eksplisitte unntak.

Slike unntak skal bare brukes når gruppen har vurdert advarselen og kan
begrunne hvorfor den ikke representerer et problem i den aktuelle
situasjonen. Unntak skal være så avgrensede som mulig slik at andre
SpotBugs-funn fortsatt blir rapportert.

## Maven og automatisert kvalitetssikring

Maven brukes som felles verktøy for bygging, testing og kontroll av
kodekvalitet. Dette gjør at de samme kontrollene kan kjøres uavhengig av
hvilken IDE eller utviklingsplattform et gruppemedlem bruker.

Den viktigste samlede kvalitetssjekken kjøres fra `bithub/` med:

`mvn clean verify`

Denne kommandoen bygger prosjektet, kjører testene og gjennomfører de
konfigurerte kontrollene for kodekvalitet. JaCoCo genererer rapport 
om testdekning under testkjøringen, mens Checkstyle og SpotBugs 
gjennomfører sine kontroller i `verify`-fasen.

Ved å samle kontrollene i Maven reduseres risikoen for at forskjellige
gruppemedlemmer bruker ulike metoder for å kontrollere prosjektet.

## Kvalitetskontroll før sammenslåing

Før en Pull Request ferdigstilles, hentes siste versjon av `master` inn
i branchen. Eventuelle konflikter løses før prosjektet kontrolleres på
nytt.

Deretter kjøres `mvn clean verify` for å kontrollere at endringene
fungerer sammen med den nyeste versjonen av prosjektet. Nye og
eksisterende tester skal bestå, og Checkstyle og SpotBugs skal ikke
rapportere feil som hindrer bygget.

Relevant dokumentasjon skal også oppdateres dersom endringen påvirker
arkitektur, funksjonalitet eller arbeidsflyt.

## Håndtering av advarsler og unntak

Advarsler og feil fra testene eller kodekvalitetsverktøyene skal som
hovedregel undersøkes og rettes fremfor å ignoreres.

Dersom det er nødvendig å legge inn et unntak i et kvalitetsverktøy,
skal gruppen først vurdere hvorfor advarselen oppstår og om koden kan
endres på en bedre måte. Unntak skal være så avgrensede som mulig og
kun brukes når gruppen kan begrunne valget.

Dette gjør at Checkstyle, SpotBugs og de andre kvalitetskontrollene
fortsetter å fungere som reelle kvalitetssjekker og ikke bare som krav
som omgås for å få bygget til å bestå.