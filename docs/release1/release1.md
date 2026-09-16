# Dokumentasjon for release 1  
 
 
 
Denne filen forklarer hvilke valg vi har tatt for den første innleveringen, men også arbeidet og utfordringene vi støtte på underveis.  
 
 
 
## Funksjonalitet i release 1  
 
  
 
Målet for første innlevering var å sette på plass et solid utgangspunkt for BIThub. BIThub er en app der vi har lagt inn følgende:  
 
- Domenemodell for [emner](/core/src/main/java/core/Course.java) og  [anmeldelser](/core/src/main/java/core/Review.java) 
- Validering av fagkode, fagnavn, navn på anmelder og     vurdering (1-5)  
- [Emneoversikt](/ui/src/main/resources/ui/App.fxml) i JavaFX der man kan velge et emne  
- [FileHandler](/core/src/main/java/core/FileHandler.java) som kan lese og skrive linjer i CSV-filer   
- Lesing av anmeldelser fra fil  
- Tester for domenelogikk og brukergrensesnitt  
- Testdekningsrapport med JaCoCo  
 
Vi er fornøyde med å ha fått på plass alle disse funksjonene i release 1, og synes vi har et godt utgangspunkt for neste del av prosjektet.  
 
  
 
  
 
  
 
### Skjermbilder  
 
  
 
### Arbeidsflyt  
 
Vi startet med JavaFX-malen som var anbefalt og satt derfor opp prosjektet som et flermodulprosjekt med modulene core og ui.  
 
Vi brukte GitHub issues for å fordele de ulike oppgavene. Oppgavene ble knyttet til milepælen Release1 og lagt inn i en GitHub Project-tavle. Tavlen ble brukt til å vise om oppgavene var i Backlog, klare, under arbeid, til vurdering eller ferdige.   
 
Arbeidet med de forskjellige issuene ble gjort på separate brancher. Alle endringer ble sendt inn gjennom pull requests og gjennomgått av andre gruppemedlemmer før de ble merget til master-branchen. Pull requests ble knyttet til de korresponderende issuene.  
 
  
 
### Samarbeid i gruppen  
 
Gruppen fordelte issuene så likt som mulig mellom alle medlemmene. GitHub Project-tavlen ble brukt for å holde oversikt over hvem som arbeidet med de ulike oppgavene og hvordan de lå an.   
 
Vi diskuterte kontinuerlig fremdrift, problemer og videre fordeling av arbeidet. Gruppen kontrollerte endringer og foreslo forbedringer gjennom Pull Requests før koden ble merget inn i master.   
 
Gruppen møttes 1 gang i uken på tirsdager kl. 13:30 for å diskutere hva vi har gjort, hva vi må gjøre og eventuelle utfordringer vi hadde.   
 
  
 
### Utfordringer i release 1  
 
En av utfordringene var å få en JavaFX-test til å fungere på både macOS og Windows. Testen simulerte først et museklikk på teksten til et emne. Dette fungerte på Windows-maskiner, men ikke på Mac. Testen ble derfor endret slik at den valgte emnet direkte gjennom JavaFX-komponenten og controlleren.  Vi hadde også utfordringer med oppsett av JaCoCo, men fikk etter hvert satt det opp i rot, ui og core slik at vi nå kan generere og se testdekningsgraden i index.html-filene.
 
SpotBugs ga tilbakemelding på at enkelte domeneklasser kunne arves fra samtidig som konstruktørene kunne kaste unntak. Vi endret derfor klassene Course og Review til final, siden de ikke skal arves fra.  
 
En annen utfordring var å samarbeide på kode og dokumentasjon som var avhengig av andre deler av prosjektet. Det ble lettere etter hvert som vi fikk tegnet en Figma-modell av appen og alle hadde samme ide om hvordan den skulle se ut og fungere. Det ble også lettere etter hvert som de grunnleggende domeneklassene og Maven-oppsettet ble merget til master-branchen.  
 
Utfordringene gjorde at vi fikk mer erfaring med Maven, testing, pull requests, kodegjennomgang og samarbeid i GitHub.   
 
  
 
## Valg for release 1  
 
Vi valgte et flermoduloppsett for å skille de ulike delene av prosjektet. Core inneholder domenelogikk og ui inneholder JavaFX-grensesnittet. Denne inndelingen gjør det enkelt å holde domenelogikken uavhengig av brukergrensesnittet. Dette gir oss et godt utgangspunkt for videre utvikling.   
 
Vi valgte å ha et enkelt domene i release 1. Domenet består derfor kun av Course og Review. Course representerer et emne og inneholder emnekode, emnenavn og anmeldelser.  Review representerer en anmeldelse og inneholder navn, vurdering fra 1 til 5 og en valgfri kommentar.   
 
Vi valgte CSV som filformat for den første releasen. CSV er tilstrekkelig for enkel lesing og skriving til fil, uten at det blir unødvendig omfattende. Fillagringen håndteres av FileHandler og bruker BufferedReader og BufferedWriter til å lese fra og skrive til fil.   
 
Vi valgte å ikke implementere innlogging i release 1. Innlogging ville krevd håndtering av brukere, passord og kobling mellom brukere og anmeldelser. Dette ville gjort domenet unødvendig omfattende for release 1.   
 
Vi har heller prioritert å ha noe i alle de tre arkitekturlagene, altså domenelogikk, JavaFX-grensesnitt og persistens (fillagring).   
 
  
 
## Tester og testdekning  
 
Testene kan kjøres fra rotmappen med kommandoen `mvn test` i terminalen, og JaCoCo lager en rapport over testdekningsgraden for hver modul.  
 
- [CourseTest](/core/src/test/java/core/CourseTest.java) tester opprettelse og validering av emner, at anmeldelsene blir lagt til riktig og at listen med anmeldelser ikke kan endres utenfra.  
- [ReviewTest](/core/src/test/java/core/ReviewTest.java) tester opprettelse og validering av anmeldelser.  
- [AppTest](/ui/src/test/java/ui/AppTest.java) tester at appen starter, og validerer at emnelisten ikke er tom, valg av emne oppdaterer teksten, dobbeltklikk åpner emnevisningen og spesialtilfeller håndteres.  
- [EmneControllerTest](/ui/src/test/java/ui/EmneControllerTest.java) tester at emnevisningen får riktig emne til å dukke opp.  
 
 
