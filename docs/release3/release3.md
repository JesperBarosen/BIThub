# Release 3

## Eksempeldata

Mappen [eksempeldata](eksempeldata/) inneholder tre emner, fem anmeldelser
og to testbrukere i lagringsformatet for release 3:

- [courses.json](eksempeldata/courses.json): emner og anmeldelser med
  unike id-er, eierskap og anonymitet.
- [users.json](eksempeldata/users.json): testbrukere med passordhash og salt.

Eksempeldataene fra release 2 beholdes i `docs/release2/eksempeldata/`.

### Testbrukere

| Navn | E-post | Passord |
| --- | --- | --- |
| Anna Hansen | anna@example.com | Bithub214! |
| Emil Berg | emil@example.com | Bithub214! |

Dette er fiktive brukere med offentlige testpassord.
Passordene lagres med PBKDF2-HMAC-SHA256, 600 000 iterasjoner,
16 byte salt og 32 byte hash. Salt og hash er Base64-kodet.

Anna eier to anmeldelser: én med navn i IT1901 og én anonym i TDT4100.
Emil eier to anmeldelser: én anonym i IT1901 og én med navn i IT2805.
Begge får derfor to anmeldelser under «Mine anmeldelser».

TDT4100 inneholder også én gammel anmeldelse uten eier.
Den har en fast id, `ownerEmail: null` og vises som «Gjest».
Den tilhører ingen av testbrukerne.

### Start serveren med eksempeldata

Kommandoene under er for Windows CMD. Du trenger JDK og Maven som
beskrevet i prosjektets README. Stopp en eventuell server som allerede
bruker port 8080.

Start i prosjektets rotmappe, `gr2610`.

Bygg og installer modulene lokalt:

```cmd
cd bithub
mvn clean install > "%TEMP%\bithub-214-install.txt" 2>&1
notepad "%TEMP%\bithub-214-install.txt"
```

Kontroller at bygget fullføres uten feil før du fortsetter.

Kopier eksempeldataene til en egen testmappe:

```cmd
if not exist "%TEMP%\bithub-release3-eksempeldata" mkdir "%TEMP%\bithub-release3-eksempeldata"
copy /Y "..\docs\release3\eksempeldata\courses.json" "%TEMP%\bithub-release3-eksempeldata\courses.json"
copy /Y "..\docs\release3\eksempeldata\users.json" "%TEMP%\bithub-release3-eksempeldata\users.json"
```

Kopiering overskriver eventuelle tidligere testdata i denne testmappen.
Vanlige brukerdata i `%USERPROFILE%\.bithub` berøres ikke.

Serveren kan endre datafilene ved oppstart og under bruk. Derfor brukes
kopier, slik at eksempeldataene i Git beholdes uendret. Serveren legger
automatisk til standardemner som mangler, så emnelisten vil inneholde
flere enn de tre emnene i eksempelfilen.

Sett filstiene og start serveren fra `bithub`:

```cmd
set "BITHUB_REVIEWS_FILE=%TEMP%\bithub-release3-eksempeldata\courses.json"
set "BITHUB_USERS_FILE=%TEMP%\bithub-release3-eksempeldata\users.json"
mvn -pl springboot/restserver spring-boot:run > "%TEMP%\bithub-214-server.txt" 2>&1
```

Miljøvariablene setter Spring-innstillingene `bithub.reviews-file` og
`bithub.users-file` i dette CMD-vinduet.

Serverkommandoen kjører til den stoppes med Ctrl+C.
Åpne loggen fra et annet CMD-vindu:

```cmd
notepad "%TEMP%\bithub-214-server.txt"
```

Åpne et nytt CMD-vindu i prosjektets rotmappe og start klienten:

```cmd
cd bithub
mvn -pl ui javafx:run > "%TEMP%\bithub-214-ui.txt" 2>&1
```

Klienten bruker REST-serveren på `http://localhost:8080`.

### Kontroller eksempeldataene

1. Logg inn som `anna@example.com` med passordet `Bithub214!`.
2. Åpne «Mine anmeldelser». Anna skal ha to anmeldelser, i IT1901 og TDT4100.
3. Les anmeldelsene i IT1901. Anna skal vises med navn og Emil som «Anonym».
4. Les anmeldelsene i TDT4100. Anna skal vises som «Anonym», og anmeldelsen
   uten eier skal vises som «Gjest».
5. Logg ut og inn som `emil@example.com` med passordet `Bithub214!`.
6. Åpne «Mine anmeldelser». Emil skal ha to anmeldelser, i IT1901 og IT2805.
   Annas anmeldelser og gjesteanmeldelsen skal ikke være med.
7. Kontroller serverloggen for feil ved lesing av filene og innlogging.

For å tilbakestille testdataene: stopp serveren, kopier begge JSON-filene
på nytt og start serveren igjen.