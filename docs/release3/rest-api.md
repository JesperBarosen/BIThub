# REST-API for BIThub

Her beskriver vi forespørslene som REST-serveren vår i modulen `springboot/restserver` støtter. JavaFX-klienten bruker bare disse forespørslene for å hente og lagre data. Vi oppdaterer dokumentet hver gang et endepunkt blir lagt til eller endret.

Serveren kjører på `http://localhost:8080`. Alle stier starter med `/api`, og alle forespørsler og svar med innhold bruker JSON (`Content-Type: application/json`).

## Ansvar

- **rest** tar imot forespørsler, sjekker innlogging og gjør om mellom JSON og objektene i core. All forretningslogikk ligger i tjenestene i core (`ReviewService` og `UserService`).
- Serveren validerer alle forespørsler selv. Vi stoler ikke på at klienten har sjekket noe, fordi hvem som helst kan sende HTTP-forespørsler direkte.
- JSON-feltene i API-et er egne modeller i `springboot/restserver`. De er ikke de samme klassene som lagres i filene (`CourseDTO`, `ReviewDTO`, `UserDTO`), slik at vi kan endre lagringen uten å endre API-et.
- Passord, passordhash, salt og e-posten til eieren av en anonym anmeldelse sendes aldri ut fra serveren.

## Oversikt

| Metode | Sti | Hva | Innlogging | Status |
|---|---|---|---|---|
| GET | `/api/courses` | Alle emner | Nei | Ferdig (#151), brukes av klienten (#172) |
| GET | `/api/courses/{code}` | Ett emne med anmeldelser og gjennomsnitt | Nei | Ferdig (#151), brukes av klienten (#172) |
| POST | `/api/courses/{code}/reviews` | Ny anmeldelse | Valgfri | Ferdig (#151), eierskap og anonymitet (#176), brukes av klienten (#172) |
| PUT | `/api/reviews/{id}` | Endre egen anmeldelse | Ja | Ferdig (#177) |
| DELETE | `/api/reviews/{id}` | Slette egen anmeldelse | Ja | Ferdig (#177) |
| POST | `/api/users` | Registrere bruker | Nei | Ferdig (#174) |
| POST | `/api/login` | Logge inn | Nei | Ferdig (#174) |
| POST | `/api/logout` | Logge ut | Ja | Ferdig (#176) |
| PUT | `/api/users/{email}/password` | Bytte eget passord | Ja | Ferdig (#174), tilgangskontroll (#176) |
| GET | `/api/me/reviews` | Egne anmeldelser, også anonyme | Ja | Ferdig (#174) |

## Innlogging og token

Vi bruker en enkel tokenløsning uten egne sikkerhetsbiblioteker. Når brukeren registrerer seg eller logger inn, lager serveren et tilfeldig token (en UUID) og husker hvilken bruker det hører til. Tokenet lagres bare i minnet, så alle må logge inn på nytt når serveren starter på nytt.

Klienten sender tokenet i headeren `Authorization` på alle forespørsler som krever innlogging, og når en innlogget bruker skriver en anmeldelse:

```http
Authorization: Bearer 3f1c9a2e-7b4d-4e8a-9c1f-2d6b5a8e0f13
```

Serveren bestemmer alltid hvem brukeren er ut fra tokenet. Hvis forespørselen inneholder en e-post eller eier, brukes den ikke til å avgjøre tilgang.

- Mangler tokenet, eller er det ukjent eller logget ut, svarer serveren `401`.
- Er brukeren logget inn, men prøver å endre noe som tilhører en annen, svarer serveren `403`.

## Feilsvar

Alle feil fra serveren har samme format, slik at klienten kan vise meldingen direkte til brukeren:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Fant ikke emnet TDT4100."
}
```

| Status | Når |
|---|---|
| 200 OK | Forespørselen gikk bra, og svaret har innhold |
| 201 Created | Noe nytt ble laget (anmeldelse, bruker) |
| 204 No Content | Forespørselen gikk bra, men svaret er tomt (sletting, utlogging, passordbytte) |
| 400 Bad Request | Ugyldige data, for eksempel vurdering utenfor 1–5 eller tomt felt |
| 401 Unauthorized | Mangler innlogging, eller feil e-post eller passord ved innlogging |
| 403 Forbidden | Innlogget, men ikke eier av det som skal endres |
| 404 Not Found | Emnet, anmeldelsen eller brukeren finnes ikke |
| 409 Conflict | E-posten er allerede registrert |
| 500 Internal Server Error | Uventet feil på serveren, for eksempel at datafilen ikke kan leses |

## Valideringsregler

Reglene ligger i core (`User`, `Review` og `ReviewService`), og serveren svarer `400` når en av dem brytes.

| Felt | Gyldig når |
|---|---|
| Navn | 3–30 tegn, bare bokstaver (også æøå og andre språk), med mellomrom, bindestrek eller apostrof mellom navnedeler. `Kari Nordmann` og `Ola-Per O'Brien` er gyldige, `Al`, `K4ri` og `-Kari` er ikke |
| E-post | Ikke tom, og på formen `noe@noe.noe` uten mellomrom. `kari@stud.ntnu.no` er gyldig, `kari@ntnu` og `kari stud.ntnu.no` er ikke |
| Passord | Ikke tomt, og minst 8 tegn |
| Vurdering | Et heltall fra 1 til 5 |
| Kommentar | Kan være tom |

## Emner og anmeldelser

### GET `/api/courses`

Henter alle emner, også de uten anmeldelser.

Svar `200`:

```json
[
  {
    "code": "IT1901",
    "name": "Informatikk prosjektarbeid I",
    "averageRating": 4.5,
    "reviewCount": 2
  },
  {
    "code": "TDT4100",
    "name": "Objektorientert programmering",
    "averageRating": 0.0,
    "reviewCount": 0
  }
]
```

### GET `/api/courses/{code}`

Henter ett emne med alle anmeldelsene.

Svar `200`:

```json
{
  "code": "IT1901",
  "name": "Informatikk prosjektarbeid I",
  "averageRating": 4.5,
  "reviews": [
    {
      "id": "b7e2c1d4-5f6a-4b3c-9d8e-1a2b3c4d5e6f",
      "displayName": "Kari Nordmann",
      "authorType": "USER",
      "rating": 5,
      "comment": "Lærerikt og godt organisert."
    },
    {
      "id": "c8f3d2e5-6a7b-4c4d-8e9f-2b3c4d5e6f7a",
      "displayName": "Anonym",
      "authorType": "ANONYMOUS",
      "rating": 4,
      "comment": "Mye arbeid, men verdt det."
    },
    {
      "id": "d9a4e3f6-7b8c-4d5e-9fa0-3c4d5e6f7a8b",
      "displayName": "Gjest",
      "authorType": "GUEST",
      "rating": 3,
      "comment": "Greit emne."
    }
  ]
}
```

Vi skiller mellom tre typer anmeldelser, som vises i feltet `authorType`:

| `authorType` | Hvem skrev den | `displayName` | Kan endres og slettes |
|---|---|---|---|
| `USER` | Innlogget bruker som viser navnet sitt | Brukerens navn | Ja, av eieren |
| `ANONYMOUS` | Innlogget bruker som har valgt å være anonym | `"Anonym"` | Ja, av eieren |
| `GUEST` | Noen som ikke var logget inn | `"Gjest"` | Nei |

En anonym anmeldelse har en eier på serveren, men svaret sier ingenting om hvem det er. En gjesteanmeldelse har ingen eier. Anmeldelser fra release 2, som ble skrevet før vi hadde innlogging, regnes også som gjesteanmeldelser.

Feil: `404` hvis emnet ikke finnes.

### POST `/api/courses/{code}/reviews`

Lager en ny anmeldelse. Med token blir anmeldelsen knyttet til den innloggede brukeren, og `anonymous` bestemmer om den vises som `USER` eller `ANONYMOUS`. Uten token blir den en gjesteanmeldelse (`GUEST`), og `anonymous` brukes ikke.

Forespørsel:

```json
{
  "rating": 4,
  "comment": "Mye arbeid, men verdt det.",
  "anonymous": true
}
```

Svar `201`: anmeldelsen slik den vises offentlig, med `id`.

```json
{
  "id": "c8f3d2e5-6a7b-4c4d-8e9f-2b3c4d5e6f7a",
  "displayName": "Anonym",
  "authorType": "ANONYMOUS",
  "rating": 4,
  "comment": "Mye arbeid, men verdt det."
}
```

Feil: `400` ved ugyldig vurdering eller kommentar, `401` hvis tokenet er sendt med, men er ukjent eller logget ut, `404` hvis emnet ikke finnes.

Serveren henter eier og navn fra brukerregisteret etter at tokenet er kontrollert. Klienten
kan ikke velge eier ved å sende navn eller e-post i JSON. Anonyme anmeldelser beholder eieren
i lagringen og vises under egne anmeldelser, men offentlig vises bare «Anonym».
En sendt, ugyldig Authorization-header gir `401`; forespørselen lagres ikke som gjest.

### PUT `/api/reviews/{id}`

Endrer en egen anmeldelse. Forespørselen har samme felter som når anmeldelsen lages.

Svar `200`: den oppdaterte anmeldelsen.

Feil: `400`, `401`, `403` hvis anmeldelsen tilhører en annen eller er en gjesteanmeldelse, `404` hvis den ikke finnes.

### DELETE `/api/reviews/{id}`

Sletter en egen anmeldelse.

Svar `204`.

Feil: `401`, `403` hvis anmeldelsen tilhører en annen eller er en gjesteanmeldelse, `404` hvis den ikke finnes.

## Brukere

### POST `/api/users`

Registrerer en ny bruker og logger den inn med en gang. Svaret har samme format som ved innlogging, så klienten kan gå rett videre til hjemmesiden uten et eget innloggingskall.

Forespørsel:

```json
{
  "name": "Kari Nordmann",
  "email": "kari@stud.ntnu.no",
  "password": "Hemmelig123"
}
```

Svar `201`:

```json
{
  "token": "3f1c9a2e-7b4d-4e8a-9c1f-2d6b5a8e0f13",
  "name": "Kari Nordmann",
  "email": "kari@stud.ntnu.no"
}
```

Feil: `400` ved ugyldig navn, e-post eller passord, `409` hvis e-posten finnes fra før.

### POST `/api/login`

Forespørsel:

```json
{
  "email": "kari@stud.ntnu.no",
  "password": "Hemmelig123"
}
```

Svar `200`:

```json
{
  "token": "3f1c9a2e-7b4d-4e8a-9c1f-2d6b5a8e0f13",
  "name": "Kari Nordmann",
  "email": "kari@stud.ntnu.no"
}
```

Feil: `401` ved feil e-post eller passord. Svaret sier ikke hvilken av dem som var feil.

### POST `/api/logout`

Sletter tokenet fra `Authorization: Bearer <token>` på serveren. Forespørselen trenger ingen body. Andre innlogginger beholdes, også for samme bruker. Det slettede tokenet kan ikke brukes til beskyttede kall eller til å logge ut på nytt.

Svar `204`. Feil: `401`.

### PUT `/api/users/{email}/password`

Bytter eget passord og krever `Authorization: Bearer <token>`. Serveren finner brukeren
fra tokenet og kontrollerer at e-posten i stien tilhører samme bruker. Store og små
bokstaver i e-postadressen behandles likt. Tilgangskontrollen skjer før passordet endres.

Forespørsel:

```json
{
  "oldPassword": "Hemmelig123",
  "newPassword": "NyttHemmelig456"
}
```

Svar `204`.

Feil: `400` hvis det nye passordet er ugyldig eller det gamle er feil, `401` hvis tokenet
mangler, er ugyldig eller er logget ut, `403` hvis e-posten tilhører en annen bruker.

### GET `/api/me/reviews`

Henter anmeldelsene den innloggede brukeren har skrevet, også de anonyme. Brukeren bestemmes
av tokenet i `Authorization`-headeren. E-post i query-parametere kan ikke brukes til å hente
en annen brukers anmeldelser.

Svar `200`:

```json
[
  {
    "id": "c8f3d2e5-6a7b-4c4d-8e9f-2b3c4d5e6f7a",
    "courseCode": "IT1901",
    "courseName": "Informatikk prosjektarbeid I",
    "rating": 4,
    "comment": "Mye arbeid, men verdt det.",
    "anonymous": true
  }
]
```

Feltet `anonymous` vises bare her, til eieren selv.

Feil: `401`.

## Felles tilgangskontroll og eierskap

Kontrollere som krever innlogging bruker `AuthenticationService.requireEmail(authorization)`.
Metoden returnerer e-posten fra serverens tokenregister, eller kaster `NotLoggedInException`
som gir `401` i det felles JSON-formatet. Utlogging fjerner tokenet fra det samme registeret.

Ved endring og sletting av anmeldelser (#177) sammenligner `ReviewService` den verifiserte
e-posten med anmeldelsens lagrede `ownerEmail`. Manglende eierskap gir
`NotReviewOwnerException`, og en ukjent id gir `ReviewNotFoundException`. `ApiExceptionHandler`
gjør dem om til `403` og `404`. Dette gjelder også anonyme anmeldelser; visningsnavnet er ikke
grunnlag for tilgang. Gjesteanmeldelser har ingen eier og kan ikke endres eller slettes av noen.
