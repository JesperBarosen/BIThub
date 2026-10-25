# Persistens og filformat

Her beskriver vi hvordan BIThub lagrer emner, anmeldelser og brukere, hvordan filene ser ut, og hvordan vi hindrer at data går tapt når flere forespørsler kommer samtidig eller skrivingen feiler.

Dokumentet bygger på [persistence.md fra release 2](../release2/persistence.md). Dette er endret siden da:

- Anmeldelser har fått id, eier og anonymitet (#142).
- Brukere lagres med passordhash og salt i stedet for passord i klartekst (#148).
- En lesefeil tolkes ikke lenger som en tom fil (#147).
- All lesing og skriving er beskyttet mot samtidige oppdateringer og skrivefeil (#149).
- `UserHandler` implementerer `UserRepository` og kobles til `UserService` på REST-serveren (#155).
- Anmeldelser kan endres og slettes, og begge operasjonene er beskyttet mot samtidige oppdateringer (#177).
- Lesing og skriving flyttes til REST-serveren i release 3.

## 1. Filformat

Vi lagrer data som **JSON** med biblioteket **Jackson**. JSON er standardisert og lett å lese, og passer godt til objekter med lister, for eksempel et emne med anmeldelser. Jackson gjør om mellom JSON og egne DTO-klasser (`CourseDTO`, `ReviewDTO` og `UserDTO`). DTO-ene beskriver hvordan dataene ser ut i filene, slik at domeneklassene i `core` kan være uforanderlige og uavhengige av Jackson.

Som standard lagres dataene i mappen `.bithub` i hjemmemappen til brukeren som kjører programmet:

| Fil | Innhold | Klasse |
|---|---|---|
| `courses.json` | Emner med anmeldelser | `ReviewHandler` |
| `users.json` | Brukere med passordhash og salt | `UserHandler` |

Filene leses og skrives bare av modulen `file`. `core` kjenner bare grensesnittene `ReviewRepository` og `UserRepository`, så lagringen kan byttes ut uten å endre `core`.

På REST-serveren oppretter `RestApplication` en `UserHandler` som `UserRepository` og gir den til `UserService` gjennom Spring-beans. Standardfilen for brukere er `.bithub/users.json` i hjemmemappen til brukeren som kjører serveren. Filstien kan overstyres med Spring-innstillingen `bithub.users-file`, for eksempel for å bruke en egen testfil.

### 1.1 Emner og anmeldelser (`courses.json`)

Filen er en liste med emner (`CourseDTO`). Hvert emne har en emnekode (`courseCode`), et navn (`courseName`) og en liste med anmeldelser (`reviews`).

Hver anmeldelse (`ReviewDTO`) har disse feltene:

| Felt | Innhold |
|---|---|
| `id` | Unik id (UUID), som brukes når anmeldelsen skal endres eller slettes |
| `ownerEmail` | E-posten til brukeren som skrev den, eller `null` for gjester |
| `reviewerName` | Navnet til den som skrev den |
| `anonymous` | `true` hvis eieren vil være anonym |
| `rating` | Vurdering fra 1 til 5 |
| `comment` | Kommentar, kan være tom |

Ut fra `ownerEmail` og `anonymous` får anmeldelsen typen `USER`, `ANONYMOUS` eller `GUEST`. E-posten til eieren lagres i filen, men skal ikke sendes ut i offentlige REST-svar (se [rest-api.md](rest-api.md)).

```json
[
  {
    "courseCode": "IT1901",
    "courseName": "Informatikk prosjektarbeid I",
    "reviews": [
      {
        "id": "b7e2c1d4-5f6a-4b3c-9d8e-1a2b3c4d5e6f",
        "ownerEmail": "kari@example.com",
        "reviewerName": "Kari Nordmann",
        "anonymous": false,
        "rating": 5,
        "comment": "Lærerikt og godt organisert."
      },
      {
        "id": "c8f3d2e5-6a7b-4c4d-8e9f-2b3c4d5e6f7a",
        "ownerEmail": "ola@example.com",
        "reviewerName": "Ola Nordmann",
        "anonymous": true,
        "rating": 4,
        "comment": "Mye arbeid, men verdt det."
      },
      {
        "id": "d9a4e3f6-7b8c-4d5e-9fa0-3c4d5e6f7a8b",
        "ownerEmail": null,
        "reviewerName": "Gjest",
        "anonymous": false,
        "rating": 3,
        "comment": "Greit emne."
      }
    ]
  }
]
```

**Filer fra release 2:** anmeldelsene der har bare `reviewerName`, `rating` og `comment`. Når `ReviewHandler` leser en slik fil, får anmeldelser uten id en ny id, og filen lagres med en gang. De har ingen eier, så de blir gjesteanmeldelser.

### 1.2 Brukere (`users.json`)

Hver bruker (`UserDTO`) har navn (`name`), e-post (`email`), passordhash (`passwordHash`) og salt (`salt`). Passordet lagres aldri i klartekst. Når en bruker lages, lages et tilfeldig salt på 16 byte, og passordet hashes med PBKDF2 (`PBKDF2WithHmacSHA256`) til 32 byte. Hash og salt lagres som Base64. Ved innlogging hashes passordet på nytt med samme salt og sammenlignes med den lagrede hashen.

```json
[
  {
    "name": "Kari Nordmann",
    "email": "kari@example.com",
    "passwordHash": "23SKf/jLMMug1kkBau2K7iiewfJMK/rKtYmGua1yHh8=",
    "salt": "4jPyTznUf4Qu1aoIQzde5Q=="
  }
]
```

`UserService` gjør e-postadresser om til små bokstaver og fjerner mellomrom rundt adressen før registrering og oppslag (#152). Dermed behandles `Kari@Example.com` og `kari@example.com` som samme adresse.

`UserHandler` gjør om mellom `UserDTO` i filen og `User` i domenelaget. Ved lesing brukes den lagrede hashen og saltet til å gjenopprette brukeren, uten å hashe passordet på nytt.

## 2. Lagringsmodell: dokumentmetafor og implisitt lagring

### 2.1 Sammenligning

- **Dokumentmetafor:** Brukeren åpner, lagrer og oppretter filer selv med fildialoger, som i et tekstbehandlingsprogram. Data som ikke er lagret, går tapt når programmet lukkes.
- **Implisitt lagring:** Programmet leser og skriver data automatisk til faste steder. Brukeren ser aldri filene.

### 2.2 Vårt valg

Vi bruker fortsatt **implisitt lagring**, slik vi begrunnet i release 2. Appen er bygget rundt navigasjon med knapper som «LES ANMELDELSER» og «SKRIV ANMELDELSE», og brukeren skal aldri måtte velge en fil.

<figure>
  <img src="../../bithub/ui/images/image-5.png" alt="Hjem-skjerm" width="250">
  <figcaption><em>Figur 2.1: Hjem-skjerm med navigasjonsknapper for lesing, skriving og egne anmeldelser.</em></figcaption>
</figure>

- **Lesing:** Når brukeren velger et emne, hentes anmeldelsene i bakgrunnen og vises med gjennomsnitt og antall.
- **Skriving:** Når brukeren trykker «SEND INN», lagres anmeldelsen uten at brukeren blir spurt om filnavn.
- **Brukere:** Registrering gjennom `UserService` lagrer brukeren i `users.json`, og innlogging leser brukerdata fra samme fil.

I release 3 flytter vi lesing og skriving til REST-serveren. Serverkoblingen mellom `UserService` og `UserHandler` er på plass i #155. REST-endepunkter for registrering, innlogging og passordbytte er nå på plass i #174.

Når JavaFX-appen er koblet til serveren, sender den forespørsler, og serveren bruker `ReviewHandler` og `UserHandler`. For brukeren er lagringen fortsatt implisitt.

### 2.3 Begrensninger

- **Handlingsutløst lagring:** Data lagres når brukeren utfører en handling, for eksempel å sende inn en anmeldelse eller registrere en bruker, ikke fortløpende mens brukeren skriver.
- **Lokale filer:** Dataene ligger i filer på maskinen der serveren kjører. Med REST-serveren kan flere klienter dele samme data, men det er ingen ekte skytjeneste eller database.
- **Én serverprosess:** Beskyttelsen mot samtidige oppdateringer gjelder bare innenfor én prosess (se avsnitt 3.3).

Passord i klartekst var en begrensning i release 2. Den er løst med hash og salt i #148.

## 3. Sikker lagring

### 3.1 Samtidige oppdateringer

REST-serveren kan behandle flere forespørsler samtidig. En endring leser hele filen, endrer listen og skriver hele filen tilbake. Uten beskyttelse kan to forespørsler lese samme fil, og den som skriver sist, overskriver endringen til den andre.

Derfor går all lesing og skriving gjennom `JsonFileStorage`:

- Hver fil har én lås (`ReentrantReadWriteLock`), som deles av alle instanser som bruker samme fil. To `ReviewHandler`-objekter med samme fil bruker altså samme lås.
- En endring holder skrivelåsen gjennom hele operasjonen: les, endre og skriv. Ingen andre kan lese eller skrive filen imens.
- Lesing uten endring bruker leselåsen, slik at flere kan lese samtidig.
- Alle metoder som endrer data, også endring og sletting av anmeldelser (#177), gjør hele operasjonen inne i `JsonFileStorage.update` og skriver med `JsonFileStorage.write`.

`UserHandler` implementerer `UserRepository`. Metoden `save` bruker `addUserIfEmailAvailable`, som sjekker om e-postadressen finnes og lagrer brukeren under samme skrivelås. Hvis adressen allerede er registrert, kaster `save` en `DuplicateEmailException`.

`UserService` gjør også en forhåndssjekk med `findByEmail`, men denne sjekken alene beskytter ikke mot samtidige registreringer. Den samlede sjekken og lagringen i `UserHandler` sikrer at to samtidige registreringer med samme e-post ikke begge lykkes.

`ReviewHandler` endrer og sletter anmeldelser på samme måte. `updateReview` og `deleteReview` finner anmeldelsen, endrer eller fjerner den og skriver filen under samme skrivelås, og svarer `false` hvis id-en ikke finnes. `ReviewService` sjekker eierskapet før den kaller lagringen. Det er trygt selv om sjekken skjer utenfor låsen, fordi eieren og id-en til en anmeldelse aldri endres. Blir anmeldelsen slettet mellom sjekken og endringen, svarer lagringen `false`, og serveren svarer `404`.

### 3.2 Skrivefeil og lesefeil

Vi skriver aldri rett til den opprinnelige filen. `JsonFileStorage.write` skriver først til en midlertidig fil i samme mappe, og flytter den deretter over den opprinnelige filen. Flyttingen er atomisk der filsystemet støtter det.

Feiler skrivingen til den midlertidige filen, for eksempel fordi disken er full eller dataene ikke kan gjøres om til JSON, er den opprinnelige filen uendret. Den midlertidige filen forsøkes slettet ved feil. Hvis filsystemet ikke støtter atomisk flytting, kan vi ikke gi samme garanti ved feil under selve erstatningen av originalfilen.

Kan en fil ikke leses på grunn av en I/O-feil, kastes `UncheckedIOException` (#147). Appen og serveren tolker aldri en lesefeil som en tom fil, fordi neste lagring da ville slettet alt som var lagret.

### 3.3 Begrensning: én serverprosess

Låsene finnes bare i minnet til én Java-prosess. Kjører to prosesser mot samme fil samtidig, for eksempel to REST-servere, eller REST-serveren og en JavaFX-app som skriver direkte til filen, er de ikke beskyttet mot hverandre.

Vi kjører derfor bare én REST-server. Når JavaFX-appen henter og lagrer data gjennom REST-serveren, er det bare serveren som skriver til filene. Skal flere prosesser dele dataene senere, må vi bytte til en database eller en fil-lås på tvers av prosesser.

## 4. Tester

Alle testene som skriver til fil, bruker `@TempDir` fra JUnit, så de skriver aldri til den ekte `~/.bithub`-mappen.

- `ReviewHandlerTest` og `UserHandlerTest` tester lesing, skriving, ødelagt JSON, lesefeil og samtidige oppdateringer fra flere instanser og tråder. `ReviewHandlerTest` tester også endring og sletting av anmeldelser, også når de skjer samtidig (#177).
- `JsonFileStorageTest` tester at skriving går via en midlertidig fil, og at den opprinnelige filen er uendret når skrivingen feiler.
- `RestApplicationTest` tester at Spring kobler brukerlagringen til `UserHandler`, og at registrering gjennom `UserService` skriver til den konfigurerte testfilen. Testen leser brukeren tilbake med en ny handler, kontrollerer at hash og salt er bevart, og tester innlogging med de lagrede brukerdataene.