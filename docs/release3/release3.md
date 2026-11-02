# Release 3

## Eksempeldata

Mappen [eksempeldata](eksempeldata/) inneholder tre emner, fem anmeldelser og to
testbrukere i lagringsformatet for release 3. Med dem kan du logge inn og se
innhold under «Mine anmeldelser» uten å registrere brukere og skrive
anmeldelser først.

- [courses.json](eksempeldata/courses.json): emner og anmeldelser med unike
  id-er, eier og om anmeldelsen er anonym.
- [users.json](eksempeldata/users.json): testbrukere med passordhash og salt.

Eksempeldataene fra release 2 ligger fortsatt i `docs/release2/eksempeldata/`.

| Emne | Anmeldelser |
| --- | --- |
| IT1901 Informatikk prosjektarbeid I | Anna med navn, Emil anonym |
| TDT4100 Objektorientert programmering | Anna anonym, én gammel uten eier |
| IT2805 Webteknologi | Emil med navn |

### Testbrukere

| Navn | E-post | Passord |
| --- | --- | --- |
| Anna Hansen | anna@example.com | Bithub214! |
| Emil Berg | emil@example.com | Bithub214! |

Dette er fiktive brukere med offentlige testpassord. Passordene lagres som hash
med PBKDF2-HMAC-SHA256, 600 000 iterasjoner, 16 byte salt og 32 byte hash, begge
Base64-kodet.

Anmeldelsen uten eier i TDT4100 viser hvordan anmeldelser fra release 2 vises
etter at innlogging ble innført. Den har `ownerEmail: null`, vises som «Gjest»
og kan ikke endres av noen.

### Bruke eksempeldataene

Serveren leser og skriver `courses.json` og `users.json` i mappen `.bithub` i
hjemmemappen din. For å bruke eksempeldataene kopierer du begge filene dit før
du starter serveren. Har du allerede filer der, ta en kopi av dem først, for de
blir overskrevet.

Fra rotmappen i repoet, på macOS, Linux og i Eclipse Che:

```bash
mkdir -p ~/.bithub
cp docs/release3/eksempeldata/courses.json docs/release3/eksempeldata/users.json ~/.bithub/
```

På Windows (PowerShell), fra rotmappen i repoet:

```powershell
New-Item -ItemType Directory -Force "$HOME\.bithub"
Copy-Item docs\release3\eksempeldata\courses.json, docs\release3\eksempeldata\users.json "$HOME\.bithub\"
```

`$HOME` skal stå akkurat slik. PowerShell bytter det ut med hjemmemappen din.

Start så serveren og appen som beskrevet i [README](/README.md). Serveren
legger til standardemner som mangler, så emnelisten inneholder flere emner enn
de tre i eksempelfilen.

For å starte på nytt med de opprinnelige dataene: stopp serveren, kopier begge
filene på nytt og start serveren igjen.

### Prøv eksempeldataene

1. Logg inn som `anna@example.com` med passordet `Bithub214!`.
2. Åpne «Mine anmeldelser». Anna har to anmeldelser, i IT1901 og TDT4100.
3. Les anmeldelsene i IT1901. Anna vises med navn og Emil som «Anonym».
4. Les anmeldelsene i TDT4100. Anna vises som «Anonym», og anmeldelsen uten
   eier vises som «Gjest».
5. Logg ut og inn som `emil@example.com` med samme passord.
6. Åpne «Mine anmeldelser». Emil har to anmeldelser, i IT1901 og IT2805.