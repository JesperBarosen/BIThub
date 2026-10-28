# Pakkestruktur

Dette dokumentet forklarer hvordan klassene i BIThub er delt inn i pakker, og hvorfor. Inndelingen ble innført i #204. Før det lå nesten alle klassene i én pakke per modul, og det var vanskelig å se hva som var kontrollere, klienter, unntak og hjelpeklasser.

## Regler vi fulgte

- **Pakker etter ansvar.** Klasser som gjør samme type jobb ligger i samme pakke, for eksempel alle kontrollerne i `ui.controller`.
- **Samme navn på tvers av modulene.** Samme type klasse har samme pakkenavn i alle modulene. Unntak ligger for eksempel i `exception` både i `core` og i `springboot/restserver`.
- **Inngangspunktene ligger øverst.** Klassene som binder modulen sammen ligger i rotpakken, slik at de er lette å finne. Det gjelder `App` og `Navigation` i `ui`, og `ReviewHandler` og `UserHandler` i `file`.
- **Testene speiler koden.** En test ligger i samme pakke som klassen den tester, for eksempel `core.service.ReviewServiceTest` for `core.service.ReviewService`.
- **Engelske pakkenavn.** Pakkene følger samme regel som resten av koden: Navn i koden er på engelsk, mens kommentarer og tekst brukeren ser er på norsk.

## core

| Pakke | Innhold | Hvorfor |
|---|---|---|
| `core.model` | `Course`, `Review`, `User` og `AuthorType` | Domeneklassene som beskriver dataene i appen. De har ingen avhengigheter til lagring eller brukergrensesnitt. |
| `core.service` | `ReviewService`, `UserService`, `PasswordHasher` og `DefaultCourses` | Logikken og reglene, for eksempel at bare eieren kan endre en anmeldelse. Både `ui` og serveren bruker disse. |
| `core.repository` | `ReviewRepository` og `UserRepository` | Grensesnittene for lagring. `core` vet bare at data kan lagres, ikke hvordan. Implementasjonene ligger i `file` og `ui.client`. |
| `core.exception` | `DuplicateEmailException`, `ReviewNotFoundException` og `NotReviewOwnerException` | Unntak fra domeneregler, som serveren gjør om til riktige HTTP-statuser. |

## file

| Pakke | Innhold | Hvorfor |
|---|---|---|
| `file` | `ReviewHandler` og `UserHandler` | Implementerer `ReviewRepository` og `UserRepository` med JSON-filer. Dette er klassene andre moduler bruker. |
| `file.dto` | `CourseDTO`, `ReviewDTO` og `UserDTO` | Formatet dataene har i JSON-filene. De holdes adskilt fra domeneklassene, slik at filformatet kan endres uten å endre `core`. |
| `file.storage` | `JsonFileStorage` og `DataHandler` | Trygg lesing og skriving av filene, med låsing og atomisk skriving (#149). |

## springboot/restserver

| Pakke | Innhold | Hvorfor |
|---|---|---|
| `rest` | `RestApplication` | Starter Spring Boot-serveren. |
| `rest.api` | Kontrollerne for endepunktene og `ApiExceptionHandler` | Tar imot HTTP-forespørsler og gjør unntak om til feilsvar. |
| `rest.auth` | `AuthenticationService` og `TokenService` | Innlogging og sjekk av token. |
| `rest.model` | Forespørsler og svar, for eksempel `LoginRequest` og `ReviewResponse` | JSON-formatet i REST-API-et, beskrevet i [rest-api.md](rest-api.md). |
| `rest.exception` | `CourseNotFoundException`, `ForbiddenException`, `InvalidCredentialsException` og `NotLoggedInException` | Unntak som bare finnes på serveren og som hører til en bestemt HTTP-status. |

## ui

| Pakke | Innhold | Hvorfor |
|---|---|---|
| `ui` | `App` og `Navigation` | `App` starter appen, og `Navigation` åpner skjermene og gir kontrollerne det de trenger. |
| `ui.controller` | Alle `*Controller`-klassene | Én kontroller per skjerm, koblet til FXML-filene i `src/main/resources/ui`. |
| `ui.client` | `RemoteReviewRepository`, `RemoteAuthenticationClient`, `AuthenticationClient` og `AuthenticationResult` | Alt som snakker med REST-serveren. Kontrollerne kjenner bare grensesnittene, ikke HTTP. |
| `ui.session` | `UserSession` | Hvem som er logget inn. Den deles av alle skjermene. |
| `ui.util` | `BackgroundTaskRunner` | Hjelpeklasse for å kjøre serverkall i bakgrunnen, beskrevet i [javafx.md](javafx.md). |

FXML-filene og `Style.css` ligger fortsatt samlet i `src/main/resources/ui`. De er ressurser og ikke Java-klasser, og det er enklere å finne dem på ett sted.

## Moduloppsett

Hver modul har en `module-info.java` som bestemmer hvilke pakker andre moduler får bruke:

- `core` eksporterer alle fire pakkene, siden både `file`, `ui` og serveren bruker dem.
- `file` åpner `file.dto` for Jackson, som leser og skriver DTO-ene med refleksjon.
- `ui` åpner `ui` og `ui.controller` for JavaFX, som lager kontrollerne fra FXML-filene.