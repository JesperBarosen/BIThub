# Persistens og filformat

Dette dokumentet beskriver hvordan data lagres i applikasjonen, samt strukturen på filformatet som brukes for persistens med JSON og Jackson.


## 1. Dokumentasjon av filformat

For å lagre brukerdata (kurs og anmeldelser) har vi valgt å bruke **JSON** (JavaScript Object Notation) i kombinasjon med **Jackson**-biblioteket i Java. 

* **Hvorfor JSON?** JSON er et menneskevennlig, standardisert og lettleselig format som passer perfekt til objektorienterte strukturer. Det gjør det enkelt å serialisere våre DTO-er (som `CourseDTO`) direkte til fil og deserialisere dem tilbake til Java-objekter.

### 1.1 Review
Filformatet består av en JSON-liste (`array`) der hvert element representerer et kurs (`CourseDTO`). Hvert kurs inneholder en emnekode (`courseCode`), et emnenavn (`courseName`), og en liste over tilhørende anmeldelser (`reviews`). Hver anmeldelse inneholder informasjon om hvem som har skrevet den (`reviewerName`), en tallkarakter (`rating`), og en tekstkommentar (`comment`).

#### Eksempel på faktisk filstruktur (`courses_data.json`)
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


### 2. Dokumentmetafor (desktop) vs implisitt lagring (app)
