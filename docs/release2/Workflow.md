<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">1. Commit-melding</h1>
<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Mal</u></span>
```text
Kort oppsummering av endringen i presens

[Valgfri utdypende beskrivelse/body av hva som er gjort og hvorfor]

[Co-authored-by:Navn]

[ref/closes/fixes] #<issue-nummer>
```

### Regler for nøkkelordene nederst
* `ref`: Vanlig endring / arbeid knyttet til et issue uten å lukke det.
* `closes` / `resolves`: Siste commit som ferdigstiller og lukker issuet.
* `fixes`: Fikser en feil/bug knyttet til issuet.

<br>

## <span style="color:#E2E8F0"><u>Eksempel</u></span>
```text
Endre farge og tekststørrelse på primærknapper

Knappene hadde for lav kontrast i mørk modus. Har oppdatert 
designsystemet slik at UU-kravene for kontrast blir ivaretatt.

Co-authored-by: Jesper

fixes #108
```

</div>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">2. Branch-navngivning</h1>
<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Mal</u></span>
```text
<type>/#<issue-nummer>-<kort-beskrivelse-med-bindestrek>
```


### Tillatte typer for branch
* `feature` (for nye funksjoner)
* `bugfix` (for feilretting)
* `refactor` (for opprydding i kode)
* `docs` (for dokumentasjonsarbeid)
* `hotfix` (for kritiske produksjonsfeil)

<br>

## <span style="color:#E2E8F0"><u>Eksempler</u></span>
```text
feature/#42-vipps-innlogging
bugfix/#108-fix-knapp-farge
hotfix/#201-kritisk-databasefeil
```

</div>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">3. Issue-beskrivelse</h1>
<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Mal</u></span>
```markdown
## Beskrivelse
En kort og tydelig oppsummering av hva som skal gjøres eller hvilken feil som oppstår.

## Motivasjon / Hvorfor
Hvorfor trenger vi denne funksjonen, eller hvilken verdi gir den brukeren?

## Konkrete oppgaver (Acceptance Criteria)
- [ ] Oppgave 1
- [ ] Oppgave 2

## Tilleggsinformasjon
Skjermbilder, skisser, stacktrace eller lenker til ekstern dokumentasjon.
```

### <span style="color:#94A3B8">Slik ser det ut:</span>
<div style="background-color: #0D0D0D; padding: 16px; border-radius: 6px; border: 1px solid #444; margin-top: 10px; margin-bottom: 24px;">

## Beskrivelse
En kort og tydelig oppsummering av hva som skal gjøres eller hvilken feil som oppstår.

## Motivasjon / Hvorfor
Hvorfor trenger vi denne funksjonen, eller hvilken verdi gir den brukeren?

## Konkrete oppgaver (Acceptance Criteria)
- [ ] Oppgave 1
- [ ] Oppgave 2

## Tilleggsinformasjon
Skjermbilder, skisser, stacktrace eller lenker til ekstern dokumentasjon.

</div>

## 5. Anbefalte Labels for Issues
Her er en oversikt over standard labels som bør brukes på issues for å holde prosjekttavlen organisert og oversiktlig:

* `bug`: Noe som er ødelagt eller ikke fungerer som forventet i applikasjonen.
* `enhancement`: Ny funksjonalitet eller forbedring av eksisterende funksjoner.
* `documentation`: Endringer, oppdateringer eller tillegg i dokumentasjon.
* `good first issue`: Enklere oppgaver som passer spesielt godt for nye bidragsytere eller ferske teammedlemmer.
* `refactor`: Opprydding eller forbedring av kodebase uten at det endrer synlig funksjonalitet.
* `dependencies`: Oppdatering av eksterne biblioteker, verktøy eller rammeverk.
<br>

## <span style="color:#E2E8F0"><u>Eksempel</u></span>
```markdown
## Beskrivelse
Vi mangler mulighet for at brukere kan logge inn med Vipps på utsjekk-siden.

## Motivasjon / Hvorfor
Mange brukere faller av i utsjekken fordi de ikke ønsker å fylle ut detaljer manuelt. Vipps vil redusere friksjon og sannsynligvis øke konverteringsraten.

## Konkrete oppgaver (Acceptance Criteria)
- [ ] Legge til Vipps-knapp under "Betalingsmetoder"
- [ ] Integrere mot Vipps Login API
- [ ] Vise en feilmelding dersom Vipps-APIet er nede
- [ ] Skrive enhetstester for innloggingsflyten

## Tilleggsinformasjon
Lenke til design i Figma: [https://figma.com/file/eksempel](https://figma.com/file/eksempel)...
```

### <span style="color:#94A3B8">Slik ser det ut:</span>
<div style="background-color: #0D0D0D; padding: 16px; border-radius: 6px; border: 1px solid #444; margin-top: 10px;">

## Beskrivelse
Vi mangler mulighet for at brukere kan logge inn med Vipps på utsjekk-siden.

## Motivasjon / Hvorfor
Mange brukere faller av i utsjekken fordi de ikke ønsker å fylle ut detaljer manuelt. Vipps vil redusere friksjon og sannsynligvis øke konverteringsraten.

## Konkrete oppgaver (Acceptance Criteria)
- [ ] Legge til Vipps-knapp under "Betalingsmetoder"
- [ ] Integrere mot Vipps Login API
- [ ] Vise en feilmelding dersom Vipps-APIet er nede
- [ ] Skrive enhetstester for innloggingsflyten

## Tilleggsinformasjon
Lenke til design i Figma: https://figma.com/file/eksempel...

</div>

</div>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">4. Pull Request (PR)</h1>
<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Mal</u></span>
```markdown
## Hva er endret?
En kort oppsummering av hva denne PR-en introduserer av endringer.

## Relaterte Issues
closes #<issue-nummer>

## Hva er gjort?
* Ting 1
* Ting 2

## Hvordan teste endringen?
1. Steg 1
2. Steg 2

## Sjekkliste før sammenslåing
- [ ] Koden er testet lokalt
- [ ] Nye og eksisterende tester kjører uten feil
```

### <span style="color:#94A3B8">Slik ser det ut:</span>
<div style="background-color: #0D0D0D; padding: 16px; border-radius: 6px; border: 1px solid #444; margin-top: 10px; margin-bottom: 24px;">

## Hva er endret?
En kort oppsummering av hva denne PR-en introduserer av endringer.

## Relaterte Issues
closes #<issue-nummer>

## Hva er gjort?
* Ting 1
* Ting 2

## Hvordan teste endringen?
1. Steg 1
2. Steg 2

## Sjekkliste før sammenslåing
- [ ] Koden er testet lokalt
- [ ] Nye og eksisterende tester kjører uten feil

</div>

<br>

## <span style="color:#E2E8F0"><u>Eksempel</u></span>
```markdown
## Hva er endret?
Lagt til støtte for Vipps-innlogging i kassa for å gi kundene et raskere alternativ.

## Relaterte Issues
closes #42

## Hva er gjort?
* Lagt til ny UI-komponent `VippsButton`
* Satt opp routing og API-kall mot Vipps Login i backend
* Lagt til enhetstester for callback-flyten

## Hvordan teste endringen?
1. Sjekk ut branchen `feature/#42-vipps-innlogging`
2. Start prosjektet lokalt
3. Gå til `/checkout`
4. Trykk på Vipps-knappen
5. Verifiser at du blir videresendt til Vipps sitt testmiljø, og deretter logget inn

## Sjekkliste før sammenslåing
- [x] Koden er testet lokalt
- [x] Nye og eksisterende tester kjører uten feil
```

### <span style="color:#94A3B8">Slik ser det ut:</span>
<div style="background-color: #0D0D0D; padding: 16px; border-radius: 6px; border: 1px solid #444; margin-top: 10px;">

## Hva er endret?
Lagt til støtte for Vipps-innlogging i kassa for å gi kundene et raskere alternativ.

## Relaterte Issues
closes #42

## Hva er gjort?
* Lagt til ny UI-komponent `VippsButton`
* Satt opp routing og API-kall mot Vipps Login i backend
* Lagt til enhetstester for callback-flyten

## Hvordan teste endringen?
1. Sjekk ut branchen `feature/#42-vipps-innlogging`
2. Start prosjektet lokalt
3. Gå til `/checkout`
4. Trykk på Vipps-knappen
5. Verifiser at du blir videresendt til Vipps sitt testmiljø, og deretter logget inn

## Sjekkliste før sammenslåing
- [x] Koden er testet lokalt
- [x] Nye og eksisterende tester kjører uten feil

</div>

</div>



<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">5. Reviews</h1>
<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## Code review

* Minst **1 godkjenning** kreves før merge til `main` (2 for kritiske deler av systemet).
* Reviewer skal sjekke:
  * At koden løser det issuet beskriver
  * At tester dekker ny/endret funksjonalitet
  * Kodestandard og lesbarhet
  * At ingen hemmeligheter eller sensitiv informasjon er inkludert
* Forventet responstid på review-forespørsler: **innen 1 arbeidsdag**.

## Definisjon of Done

En oppgave regnes som ferdig når:
- [ ] Koden er skrevet og fungerer som forventet
- [ ] Enhetstester (og ev. integrasjonstester) er skrevet og kjører grønt
- [ ] Koden er reviewet og godkjent
- [ ] Dokumentasjon er oppdatert ved behov
- [ ] PR er merget til `main`
- [ ] Tilhørende issue er lukket

</div>

</div>