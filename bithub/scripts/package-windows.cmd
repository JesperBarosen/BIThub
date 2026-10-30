@echo off
setlocal
cd /d "%~dp0.."

set "APP_VERSION=0.0.1"
set "PACKAGE_DIR=ui\target\jpackage"

if not defined BITHUB_SERVER_URL set "BITHUB_SERVER_URL=http://localhost:8080"

rem Finn WiX dersom installasjonsmappen ikke ligger i PATH.
if exist "%ProgramFiles(x86)%\WiX Toolset v3.14\bin\candle.exe" (
    set "PATH=%PATH%;%ProgramFiles(x86)%\WiX Toolset v3.14\bin"
)

for %%T in (mvn jlink jpackage candle light) do (
    where %%T >nul 2>&1
    if errorlevel 1 (
        echo FEIL: %%T ble ikke funnet i PATH.
        exit /b 1
    )
)

echo Bygger JavaFX-klienten...
call mvn -pl ui -am clean install -DskipTests
if errorlevel 1 exit /b 1

echo Lager tilpasset Java-runtime...
call mvn -pl ui javafx:jlink
if errorlevel 1 exit /b 1

if not exist "%PACKAGE_DIR%" mkdir "%PACKAGE_DIR%"

echo Lager Windows-applikasjonen...
jpackage --type app-image --name BIThub --app-version "%APP_VERSION%" --vendor "gr2610" --dest "%PACKAGE_DIR%" --runtime-image "ui\target\bithub" --module bithub.ui/ui.App --java-options "-Dbithub.server-url=%BITHUB_SERVER_URL%"
if errorlevel 1 exit /b 1

echo Lager Windows-installasjonsfilen...
jpackage --type exe --name BIThub --app-version "%APP_VERSION%" --vendor "gr2610" --app-image "%PACKAGE_DIR%\BIThub" --dest "%PACKAGE_DIR%" --win-menu --win-shortcut
if errorlevel 1 exit /b 1

echo.
echo Windows-installasjonen er klar:
echo %PACKAGE_DIR%\BIThub-%APP_VERSION%.exe
exit /b 0
