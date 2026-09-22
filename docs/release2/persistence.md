# Persistens og filformat

Dette dokumentet beskriver hvordan data lagres i applikasjonen, samt
strukturen på filformatet som brukes for persistens med JSON og Jackson.

## 1. Dokumentasjon av filformat

For å lagre applikasjonsdata, som kurs, anmeldelser og brukerdata, har vi
valgt å bruke **JSON** (JavaScript Object Notation) i kombinasjon med
**Jackson**-biblioteket i Java.

* **Hvorfor JSON?** JSON er et menneskevennlig, standardisert og lettleselig
  format som passer godt til objektorienterte datastrukturer. Det gjør det
  enkelt å serialisere DTO-er, som `CourseDTO` og `UserDTO`, direkte til fil
  og deserialisere dem tilbake til Java-objekter.

### 1.1 Kurs og anmeldelser

Kurs og anmeldelser lagres som en JSON-liste (`array`) der hvert element
representerer et kurs (`CourseDTO`).

Hvert kurs inneholder en emnekode (`courseCode`), et emnenavn (`courseName`)
og en liste over tilhørende anmeldelser (`reviews`).

Hver anmeldelse inneholder informasjon om hvem som har skrevet den
(`reviewerName`), en tallkarakter (`rating`) og en tekstkommentar (`comment`).

#### Eksempel på filstruktur for kurs og anmeldelser

```json
[
  {
    "courseCode": "IT1901",
    "courseName": "Informatikk prosjektarbeid",
    "reviews": [
      {
        "reviewerName": "Student",
        "rating": 2,
        "comment": "KOMMENTAR"
      },
      {
        "reviewerName": "Anonym",
        "rating": 4,
        "comment": "KOMMENTAR"
      }
    ]
  },
  {
    "courseCode": "TDT4100",
    "courseName": "Objektorientert programmering",
    "reviews": [
      {
        "reviewerName": "Anonym",
        "rating": 3,
        "comment": "KOMMENTAR"
      }
    ]
  }
]

```
### 1.2 Brukerdata

Brukerdata lagres i en egen JSON-fil. Hver bruker representeres av en
`UserDTO`, som inneholder navn (`name`), e-postadresse (`email`) og passord
(`password`).

`UserHandler` håndterer lesing og skriving av brukerdata ved hjelp av Jackson.
Dersom filen ikke finnes eller er tom, returneres en tom liste. Ved skriving
opprettes lagringsmappen automatisk dersom den ikke allerede finnes.

Standard lagringsplassering for brukerdata er `.bithub/users.json` i
brukerens hjemmemappe.

#### Eksempel på filstruktur (`users.json`)

```json
[
  {
    "name": "Stine Sørnes",
    "email": "stine@example.com",
    "password": "password123"
  },
  {
    "name": "Ola Nordmann",
    "email": "ola@example.com",
    "password": "anotherPassword"
  }
]

```

### 2. Dokumentmetafor (desktop) vs implisitt lagring (app)

Applikasjonen bruker implisitt lagring fremfor en tradisjonell
dokumentmetafor. Brukeren trenger derfor ikke å åpne, velge eller lagre
JSON-filer manuelt.

Persistenslaget håndterer lesing og skriving av data når applikasjonen
trenger å hente eller lagre informasjon. Data lagres i applikasjonens egne
JSON-filer under `.bithub` i brukerens hjemmemappe.

Dette gjør at filhåndteringen skjules for brukeren, mens applikasjonen selv
har ansvar for å lese eksisterende data og skrive oppdaterte data tilbake
til fil.