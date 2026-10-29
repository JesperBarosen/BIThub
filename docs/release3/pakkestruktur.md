# Pakkestruktur

Dette dokumentet forklarer hvordan klassene i BIThub er delt inn i pakker, og hvorfor. Inndelingen ble innført i #204. Før det lå nesten alle klassene i én pakke per modul, og det var vanskelig å se hva som var kontrollere, klienter, unntak og hjelpeklasser.

## Regler vi fulgte

- **Pakker etter ansvar.** Klasser som gjør samme type jobb ligger i samme pakke, for eksempel alle kontrollerne i [`ui.controller`](../../bithub/ui/src/main/java/ui/controller).
- **Samme navn på tvers av modulene.** Samme type klasse har samme pakkenavn i alle modulene. Unntak ligger for eksempel i `exception` både i `core` og i [`springboot/restserver`](../../bithub/springboot/restserver).
- **Inngangspunktene ligger øverst.** Klassene som binder modulen sammen ligger i rotpakken, slik at de er lette å finne. Det gjelder [`App`](../../bithub/ui/src/main/java/ui/App.java) og [`Navigation`](../../bithub/ui/src/main/java/ui/Navigation.java) i `ui`, og [`ReviewHandler`](../../bithub/file/src/main/java/file/ReviewHandler.java) og [`UserHandler`](../../bithub/file/src/main/java/file/UserHandler.java) i `file`.
- **Testene speiler koden.** En test ligger i samme pakke som klassen den tester, for eksempel [`core.service.ReviewServiceTest`](../../bithub/core/src/test/java/core/service/ReviewServiceTest.java) for [`core.service.ReviewService`](../../bithub/core/src/main/java/core/service/ReviewService.java).
- **Engelske pakkenavn.** Pakkene følger samme regel som resten av koden: Navn i koden er på engelsk, mens kommentarer og tekst brukeren ser er på norsk.

## core

| Pakke | Innhold | Hvorfor |
|---|---|---|
| [`core.model`](../../bithub/core/src/main/java/core/model) | [`Course`](../../bithub/core/src/main/java/core/model/Course.java), [`Review`](../../bithub/core/src/main/java/core/model/Review.java), [`User`](../../bithub/core/src/main/java/core/model/User.java) og [`AuthorType`](../../bithub/core/src/main/java/core/model/AuthorType.java) | Domeneklassene som beskriver dataene i appen. De har ingen avhengigheter til lagring eller brukergrensesnitt. |
| [`core.service`](../../bithub/core/src/main/java/core/service) | [`ReviewService`](../../bithub/core/src/main/java/core/service/ReviewService.java), [`UserService`](../../bithub/core/src/main/java/core/service/UserService.java), [`PasswordHasher`](../../bithub/core/src/main/java/core/service/PasswordHasher.java) og [`DefaultCourses`](../../bithub/core/src/main/java/core/service/DefaultCourses.java) | Logikken og reglene, for eksempel at bare eieren kan endre en anmeldelse. Både `ui` og serveren bruker disse. |
| [`core.repository`](../../bithub/core/src/main/java/core/repository) | [`ReviewRepository`](../../bithub/core/src/main/java/core/repository/ReviewRepository.java) og [`UserRepository`](../../bithub/core/src/main/java/core/repository/UserRepository.java) | Grensesnittene for lagring. `core` vet bare at data kan lagres, ikke hvordan. Implementasjonene ligger i `file` og [`ui.client`](../../bithub/ui/src/main/java/ui/client). |
| [`core.exception`](../../bithub/core/src/main/java/core/exception) | [`DuplicateEmailException`](../../bithub/core/src/main/java/core/exception/DuplicateEmailException.java), [`ReviewNotFoundException`](../../bithub/core/src/main/java/core/exception/ReviewNotFoundException.java) og [`NotReviewOwnerException`](../../bithub/core/src/main/java/core/exception/NotReviewOwnerException.java) | Unntak fra domeneregler, som serveren gjør om til riktige HTTP-statuser. |

## file

| Pakke | Innhold | Hvorfor |
|---|---|---|
| `file` | [`ReviewHandler`](../../bithub/file/src/main/java/file/ReviewHandler.java) og [`UserHandler`](../../bithub/file/src/main/java/file/UserHandler.java) | Implementerer [`ReviewRepository`](../../bithub/core/src/main/java/core/repository/ReviewRepository.java) og [`UserRepository`](../../bithub/core/src/main/java/core/repository/UserRepository.java) med JSON-filer. Dette er klassene andre moduler bruker. |
| [`file.dto`](../../bithub/file/src/main/java/file/dto) | [`CourseDTO`](../../bithub/file/src/main/java/file/dto/CourseDTO.java), [`ReviewDTO`](../../bithub/file/src/main/java/file/dto/ReviewDTO.java) og [`UserDTO`](../../bithub/file/src/main/java/file/dto/UserDTO.java) | Formatet dataene har i JSON-filene. De holdes adskilt fra domeneklassene, slik at filformatet kan endres uten å endre `core`. |
| [`file.storage`](../../bithub/file/src/main/java/file/storage) | [`JsonFileStorage`](../../bithub/file/src/main/java/file/storage/JsonFileStorage.java) og [`DataHandler`](../../bithub/file/src/main/java/file/storage/DataHandler.java) | Trygg lesing og skriving av filene, med låsing og atomisk skriving (#149). |

## springboot/restserver

| Pakke | Innhold | Hvorfor |
|---|---|---|
| `rest` | [`RestApplication`](../../bithub/springboot/restserver/src/main/java/rest/RestApplication.java) | Starter Spring Boot-serveren. |
| [`rest.api`](../../bithub/springboot/restserver/src/main/java/rest/api) | Kontrollerne for endepunktene og [`ApiExceptionHandler`](../../bithub/springboot/restserver/src/main/java/rest/api/ApiExceptionHandler.java) | Tar imot HTTP-forespørsler og gjør unntak om til feilsvar. |
| [`rest.auth`](../../bithub/springboot/restserver/src/main/java/rest/auth) | [`AuthenticationService`](../../bithub/springboot/restserver/src/main/java/rest/auth/AuthenticationService.java) og [`TokenService`](../../bithub/springboot/restserver/src/main/java/rest/auth/TokenService.java) | Innlogging og sjekk av token. |
| [`rest.model`](../../bithub/springboot/restserver/src/main/java/rest/model) | Forespørsler og svar, for eksempel [`LoginRequest`](../../bithub/springboot/restserver/src/main/java/rest/model/LoginRequest.java) og [`ReviewResponse`](../../bithub/springboot/restserver/src/main/java/rest/model/ReviewResponse.java) | JSON-formatet i REST-API-et, beskrevet i [rest-api.md](rest-api.md). |
| [`rest.exception`](../../bithub/springboot/restserver/src/main/java/rest/exception) | [`CourseNotFoundException`](../../bithub/springboot/restserver/src/main/java/rest/exception/CourseNotFoundException.java), [`ForbiddenException`](../../bithub/springboot/restserver/src/main/java/rest/exception/ForbiddenException.java), [`InvalidCredentialsException`](../../bithub/springboot/restserver/src/main/java/rest/exception/InvalidCredentialsException.java) og [`NotLoggedInException`](../../bithub/springboot/restserver/src/main/java/rest/exception/NotLoggedInException.java) | Unntak som bare finnes på serveren og som hører til en bestemt HTTP-status. |

## ui

| Pakke | Innhold | Hvorfor |
|---|---|---|
| `ui` | [`App`](../../bithub/ui/src/main/java/ui/App.java) og [`Navigation`](../../bithub/ui/src/main/java/ui/Navigation.java) | [`App`](../../bithub/ui/src/main/java/ui/App.java) starter appen, og [`Navigation`](../../bithub/ui/src/main/java/ui/Navigation.java) åpner skjermene og gir kontrollerne det de trenger. |
| [`ui.controller`](../../bithub/ui/src/main/java/ui/controller) | Alle `*Controller`-klassene | Én kontroller per skjerm, koblet til FXML-filene i [`src/main/resources/ui`](../../bithub/ui/src/main/resources/ui). |
| [`ui.client`](../../bithub/ui/src/main/java/ui/client) | [`RemoteReviewRepository`](../../bithub/ui/src/main/java/ui/client/RemoteReviewRepository.java), [`RemoteAuthenticationClient`](../../bithub/ui/src/main/java/ui/client/RemoteAuthenticationClient.java), [`AuthenticationClient`](../../bithub/ui/src/main/java/ui/client/AuthenticationClient.java) og [`AuthenticationResult`](../../bithub/ui/src/main/java/ui/client/AuthenticationResult.java) | Alt som snakker med REST-serveren. Kontrollerne kjenner bare grensesnittene, ikke HTTP. |
| [`ui.session`](../../bithub/ui/src/main/java/ui/session) | [`UserSession`](../../bithub/ui/src/main/java/ui/session/UserSession.java) | Hvem som er logget inn. Den deles av alle skjermene. |
| [`ui.util`](../../bithub/ui/src/main/java/ui/util) | [`BackgroundTaskRunner`](../../bithub/ui/src/main/java/ui/util/BackgroundTaskRunner.java) | Hjelpeklasse for å kjøre serverkall i bakgrunnen, beskrevet i [javafx.md](javafx.md). |

FXML-filene og [`Style.css`](../../bithub/ui/src/main/resources/ui/Style.css) ligger fortsatt samlet i [`src/main/resources/ui`](../../bithub/ui/src/main/resources/ui). De er ressurser og ikke Java-klasser, og det er enklere å finne dem på ett sted.

## Moduloppsett

Hver modul har en `module-info.java` som bestemmer hvilke pakker andre moduler får bruke:

- `core` eksporterer alle fire pakkene, siden både `file`, `ui` og serveren bruker dem.
- `file` åpner [`file.dto`](../../bithub/file/src/main/java/file/dto) for Jackson, som leser og skriver DTO-ene med refleksjon.
- `ui` åpner `ui` og [`ui.controller`](../../bithub/ui/src/main/java/ui/controller) for JavaFX, som lager kontrollerne fra FXML-filene.