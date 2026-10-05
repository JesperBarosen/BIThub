# Kodekvalitet
 
Dette dokumentet beskriver hvordan kodekvalitet sikres i BIThub.
Prosjektet bruker automatiserte tester og kodeanalyse gjennom Maven for
å oppdage feil og opprettholde en konsistent kodebase.
 
## Testing
 
Tester brukes for å kontrollere at eksisterende funksjonalitet fortsetter
å fungere når prosjektet endres. Alle modulene har tester. Prosjektet
bruker JUnit 5 i alle modulene, og TestFX for å teste
brukergrensesnittet i `ui`.
 
Tilnærmingen er å teste hvert lag for seg, og i tillegg ha enkelte
tester som kontrollerer at lagene fungerer sammen. I `core` testes
domeneklassene og `ReviewService` med enhetstester. `ReviewServiceTest`
bruker en egen testimplementasjon av `ReviewRepository` i stedet for
ekte fillagring, slik at testene kun kontrollerer logikken i
domenelaget.
 
Persistenslaget i `file` testes for lesing, skriving og håndtering av
feiltilfeller, som tomme filer, ugyldig JSON og feil ved skriving.
`ReviewHandlerTest` sjekker også at et emne kan leses tilbake med riktige
anmeldelser, uten å skille store og små bokstaver i emnekoden.
For å teste feil ved lesing og skriving brukes Mockito til å simulere
at `Files` kaster `IOException`, siden slike feil er vanskelige å
fremprovosere med ekte filer. `ReviewStorageTest` kontrollerer at `ReviewService` og `ReviewHandler`
fungerer sammen, og at en anmeldelse som sendes inn faktisk blir lagret
til fil.
 
Brukergrensesnittet testes med TestFX. Testene kontrollerer
enkeltkontrollere, som feilmeldinger og innsending i `SkrivAnmeldelseController` og
visning av anmeldelser, gjennomsnitt og antall i `LeseAnmeldelserController`,
og navigasjonen mellom sidene. `NewNavigationTest` har en ende-til-ende-test
som skriver en anmeldelse i appen og leser den tilbake, slik at alle tre
lagene testes sammen. UI-testene skriver til en midlertidig fil, så de
påvirker ikke brukerens egne data i `~/.bithub`.
 
Tester som skriver til fil bruker `@TempDir` fra JUnit. Testene skriver
dermed aldri til den faktiske `~/.bithub`-mappen, og testene påvirker
ikke hverandre.
 
Testene skal kontrollere forventet oppførsel, grenseverdier og
feilsituasjoner. For vurderinger testes for eksempel både gyldige
grenseverdier og verdier som 0 og 6, som skal avvises.
 
Testene skal kunne kjøres gjennom Maven slik at kvalitetssikringen ikke
er avhengig av hvilken IDE et gruppemedlem bruker.
 
## Testdekning med JaCoCo
 
JaCoCo brukes for å måle hvilke deler av koden som blir kjørt av
testene. Rapporten brukes for å identifisere områder med mangelfull
testdekning, slik at det kan utvikles flere tester der det er nødvendig.
 
Prosjektet bruker JaCoCo Maven Plugin versjon 0.8.12. `prepare-agent`
kobler JaCoCo til testkjøringen, mens `report` kjøres i `test`-fasen og
genererer rapporten over testdekning.
 
Rapportene genereres for hver modul i `core/target/site/jacoco/`,
`file/target/site/jacoco/` og `ui/target/site/jacoco/`, og åpnes
gjennom `index.html`. Testdekningen for release 2 er oppsummert i
[release2.md](release2.md).
 
Det er ikke konfigurert en fast minimumsgrense for testdekning. En fast
grense kan føre til at det skrives tester bare for å øke prosenten, i
stedet for tester som kontrollerer faktisk oppførsel. JaCoCo brukes
derfor som et analyseverktøy, og testdekningen vurderes som en del av
gjennomgangen av pull requests.
 
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
Gruppen valgte fire mellomrom fordi dette er innrykket alle
gruppemedlemmene er vant til fra tidligere fag, og fordi det er
standard innrykk for Java i VS Code. Utover innrykk følger prosjektet
Googles regelsett, siden dette er et etablert og gjennomarbeidet
utgangspunkt.
 
Prosjektet bruker Maven Checkstyle Plugin versjon 3.6.0 med Checkstyle
versjon 10.26.1. Både produksjonskode og testkode kontrolleres, slik at
testene holder samme kvalitet som resten av koden. Checkstyle kjører
målet `check` i `verify`-fasen.
 
VS Code bruker samme regelsett gjennom `java.checkstyle.configuration`,
satt til `${workspaceFolder}/config/checkstyle/checkstyle.xml`.
Kodelagerets rotmappe må åpnes i VS Code for å bruke disse
prosjektinnstillingene.
 
Java-formateringen styres av `.vscode/java-formatter.xml`.
Formatet bruker fire mellomrom, mellomrom fremfor tabulatorer og
linjelengde på 100 tegn. Dette samkjører formateringen med de valgte
Checkstyle-reglene. Automatisk formatering retter mange stilbrudd,
mens blant annet manglende Javadoc må håndteres manuelt.
 
VS Code bruker formatterprofilen `JavaConventions` fra formatterfila.
HTML-formatering i Javadoc er deaktivert, fordi formatteren ellers
flytter `<p>` til en egen linje. Checkstyle krever at `<p>` står rett
foran teksten i avsnittet. Innstillingen påvirker kun hvordan
kommentaren ser ut i kildekoden, ikke hvordan Javadoc vises.
 
Checkstyle kan også kjøres separat fra `bithub/` med
`mvn checkstyle:check`.
 
Med dagens oppsett rapporteres regelbrudd på warning-nivå uten at
bygget feiler. `BUILD SUCCESS` betyr derfor ikke at koden er uten
Checkstyle-warnings. Gruppen har derfor en egen regel om at en pull
request ikke skal innføre nye Checkstyle-warnings. Den som oppretter
pull requesten kontrollerer Checkstyle-rapporten før review, og
reviewer kontrollerer at dette er gjort.
 
Eventuelle endringer i Checkstyle-konfigurasjonen skal gjøres bevisst
og begrunnes dersom de valgte reglene ikke passer prosjektet.
 
## SpotBugs
 
SpotBugs brukes til statisk analyse av Java-koden. Verktøyet analyserer
koden for å finne potensielle feil og problematiske kodemønstre som ikke
nødvendigvis blir oppdaget av kompilatoren eller testene.
 
Prosjektet bruker SpotBugs Maven Plugin versjon 4.8.6.6. Målet `check`
kjøres i `verify`-fasen og inngår dermed i den samlede
kvalitetssjekken. I motsetning til Checkstyle feiler bygget dersom
SpotBugs finner feil. Prosjektet bruker standardinnstillingene for
analysenivå og terskel.
 
SpotBugs er konfigurert til å generere HTML-rapport, siden denne er
enklere å lese enn XML. XML-output er derfor deaktivert. Prosjektet
bruker `config/spotbugs/exclude.xml` som filter for eksplisitte
unntak.
 
Prosjektet har i dag to unntak. Det første gjelder `EI_EXPOSE_REP2`
for feltet `reviewRepository` i `ReviewService`. `ReviewService` skal
bruke den samme lagringen som den får fra `App`, og ikke en kopi.
Unntaket er definert i `exclude.xml` og gjelder kun dette feltet.
 
Det andre unntaket gjelder `EI_EXPOSE_REP` for metoden `getReviews()`
i `CourseDTO`. Jackson trenger direkte tilgang til listen for å lese og
skrive JSON, og klassen er kun ment for lagring av data. Unntaket er lagt
inn med `@SuppressFBWarnings` direkte på metoden, slik at det ikke
gjelder resten av klassen.
 
Slike unntak skal bare brukes når gruppen har vurdert advarselen og kan
begrunne hvorfor den ikke representerer et problem i den aktuelle
situasjonen.
 
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
 
Før en pull request ferdigstilles, hentes siste versjon av `master` inn
i branchen. Eventuelle konflikter løses før prosjektet kontrolleres på
nytt.
 
Deretter kjøres `mvn clean verify` for å kontrollere at endringene
fungerer sammen med den nyeste versjonen av prosjektet. Bygget skal
fullføres uten feil, nye og eksisterende tester skal bestå, SpotBugs
skal ikke rapportere feil, og endringen skal ikke føre til nye
Checkstyle-warnings.
 
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