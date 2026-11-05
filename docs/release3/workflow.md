<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">Samlet workflow</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

Arbeidsflyten i release 3 er den samme som i release 2, med en endring: en pull request må nå godkjennes av minst to andre på gruppen før den merges.

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## Navigasjon

**Workflow**

* [1. Issue-beskrivelse](#issue)
* [2. Branch-navngivning](#branch)
* [3. Commit-melding](#commit)
* [4. Pull Request (PR)](#pull-request)
* [5. Code Review](#review)
* [6. Merge](#merge)
* [7. Issue lukkes](#issue-lukkes)
* [8. Ferdig](#ferdig)
* [9. Kommentarer](#Kommentarer)

**Hurtignavigasjon**

`Issue` → `Branch` → `Commit` → `PR` → `Review` → `Merge` → `Closed` → `Ferdig`

</div>

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

```text
1. ISSUE

   │
   │  Beskriv problem/funksjon
   │  + acceptance criteria
   ▼

2. BRANCH

   │
   │  feature/#42-vipps-innlogging
   ▼

3. COMMIT

   │
   │  Skriv kode + tester
   │  Lag commits med issue-referanse
   ▼

4. PULL REQUEST

   │
   │  closes #42
   │  Beskriv endringer + testing
   ▼

5. CODE REVIEW

   │
   │  Minst to andre personer godkjenner
   ▼

6. MERGE

   │
   │  Merge til master
   ▼

7. ISSUE LUKKES

   │
   │  Definition of Done er oppfylt
   ▼

8. FERDIG
```

## <span style="color:#E2E8F0"><u>Hovedregel</u></span>

> **Issue → Branch → Commit → PR → Review → Merge → Closed**

Dette gir en tydelig kobling mellom hva som skal gjøres, arbeidet som utføres, og hvordan arbeidet blir kvalitetssikret før det merges til `master`.

</div>

<a id="issue"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">1. Issue-beskrivelse</h1>

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

* [ ] Oppgave 1
* [ ] Oppgave 2

## Tilleggsinformasjon

Skjermbilder, skisser, stacktrace eller lenker til ekstern dokumentasjon.

</div>

## Anbefalte Labels for Issues

* `bug`: Noe som er ødelagt eller ikke fungerer som forventet.
* `enhancement`: Ny funksjonalitet eller forbedring.
* `documentation`: Endringer eller tillegg i dokumentasjon.
* `good first issue`: Enklere oppgaver som passer nye bidragsytere eller teammedlemmer.
* `refactor`: Opprydding eller forbedring av kode uten å endre synlig funksjonalitet.
* `dependencies`: Oppdatering av eksterne biblioteker, verktøy eller rammeverk.

## <span style="color:#E2E8F0"><u>Eksempel</u></span>

```markdown
## Beskrivelse

Vi mangler mulighet for at brukere kan logge inn med Vipps på utsjekk-siden.

## Motivasjon / Hvorfor

Mange brukere faller av i utsjekken fordi de ikke ønsker å fylle ut detaljer manuelt.

## Konkrete oppgaver (Acceptance Criteria)

- [ ] Legge til Vipps-knapp under "Betalingsmetoder"
- [ ] Integrere mot Vipps Login API
- [ ] Vise en feilmelding dersom Vipps-APIet er nede
- [ ] Skrive enhetstester for innloggingsflyten

## Tilleggsinformasjon

Lenke til design i Figma: [https://figma.com/file/eksempel](https://figma.com/file/eksempel)
```

</div>

<a id="branch"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">2. Branch-navngivning</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">


## <span style="color:#E2E8F0"><u>Regler</u></span>

* Én branch skal normalt knyttes til ett issue.
* Branch-navnet skal være kort og beskrivende.
* Bruk små bokstaver.
* Bruk bindestrek mellom ord.
* Ikke bruk mellomrom eller spesialtegn utover `/`, `#` og `-`.

## <span style="color:#E2E8F0"><u>Mal</u></span>

```bash
<type>/#<issue-nummer>-<kort-beskrivelse-med-bindestrek>
```

### Tillatte typer for branch

* `feature` – nye funksjoner
* `bugfix` – feilretting
* `hotfix` – kritiske feil som må rettes raskt
* `refactor` – opprydding i kode
* `docs` – dokumentasjonsarbeid

## <span style="color:#E2E8F0"><u>Eksempler</u></span>

```bash
feature/#42-vipps-innlogging
bugfix/#108-fix-knapp-farge
hotfix/#201-kritisk-databasefeil
docs/#55-oppdater-readme
```

</div>

<a id="commit"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">3. Commit-melding</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Regler</u></span>

Commit-meldinger skal være korte, tydelige og beskrive hva committen gjør. Skriv i presens og bruk imperativ form.

### Subject

* Skill subject fra body med en tom linje.
* Begrens subject til **50 tegn**.
* Start subject med stor bokstav.
* Ikke avslutt subject med punktum.
* Bruk imperativ form.

#### Eksempel på commit tittel
```text
Legg til validering av e-postadresse
```

### Body

* Forklar **hva** som er gjort og **hvorfor**.
* Ikke bruk body til detaljert forklaring av **hvordan** implementasjonen fungerer.
* Bryt linjer ved omtrent **72 tegn**.
* Body er valgfri.

Når en commit er knyttet til et issue, skal issue-nummeret inkluderes i commit-meldingen.

## <span style="color:#E2E8F0"><u>Mal</u></span>

```bash
Kort oppsummering av endringen i presens

[Valgfri utdypende beskrivelse av hva som er gjort og hvorfor]

[ref/closes/fixes] #<issue-nummer>

[Co-authored-by: navn <mail>]
```

### Regler for nøkkelordene nederst

* `ref`: Arbeid knyttet til et issue uten å lukke det.
* `closes` / `resolves`: Siste endring som ferdigstiller og lukker issuet.
* `fixes`: Fikser en feil/bug knyttet til issuet.

## <span style="color:#E2E8F0"><u>Eksempel</u></span>

```text
Endre farge og tekststørrelse på primærknapper

Knappene hadde for lav kontrast i mørk modus. Har oppdatert
designsystemet slik at UU-kravene for kontrast blir ivaretatt.

fixes #108

Co-authored-by: Ola Nordmann <ola.nordmann@stud.ntnu.no>
Co-authored-by: Tobias Bernard <tob.bern@stud.ntnu.no>
```

## <span style="color:#E2E8F0"><u>Kommandoer</u></span>

```bash
git add <filnavn>
git add .

git commit -m "Kort oppsummering"

# Med body:
git commit -m "Kort oppsummering" -m "Utdypende beskrivelse av hva og hvorfor"
```

</div>

<a id="pull-request"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">4. Pull Request (PR)</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Formål</u></span>

En PR brukes til å få endringer reviewet før de merges til `master`.

En PR skal:

* Være knyttet til et issue.
* Forklare hva som er endret.
* Forklare hvordan endringen kan testes.
* Ha gjennomført relevante tester.
* Være reviewet av minst to andre teammedlemmer.

## <span style="color:#E2E8F0"><u>Mal</u></span>

```markdown
## Hva er endret?

En kort oppsummering av hva denne PR-en introduserer.

## Relaterte Issues

closes #<issue-nummer>

## Hva er gjort?

- Ting 1
- Ting 2

## Hvordan teste endringen?

1. Steg 1
2. Steg 2

## Sjekkliste før review

- [ ] Koden er testet lokalt
- [ ] `mvn clean verify` fullføres uten feil
- [ ] Acceptance criteria er oppfylt
- [ ] Dokumentasjonen er gjennomgått og oppdatert der det er nødvendig
```

## <span style="color:#E2E8F0"><u>Eksempel</u></span>

```markdown
## Hva er endret?

Lagt til støtte for Vipps-innlogging i kassa.

## Relaterte Issues

closes #42

## Hva er gjort?

- Lagt til ny UI-komponent `VippsButton`
- Satt opp routing og API-kall
- Lagt til enhetstester for callback-flyten

## Hvordan teste endringen?

1. Sjekk ut branchen `feature/#42-vipps-innlogging`
2. Start prosjektet lokalt
3. Gå til `/checkout`
4. Trykk på Vipps-knappen
5. Verifiser innloggingsflyten

## Sjekkliste før review

- [x] Koden er testet lokalt
- [x] mvn clean verify fullføres uten feil
- [x] Acceptance criteria er oppfylt
- [x] Dokumentasjonen er gjennomgått og oppdatert der det er nødvendig
```

</div>

<a id="review"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">5. Code Review</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Formål</u></span>

Code review brukes for å kontrollere at endringen er korrekt, forståelig, testet og i tråd med prosjektets standarder før den merges.

## <span style="color:#E2E8F0"><u>Reviewer skal kontrollere</u></span>

* At koden løser det issuet beskriver.
* At acceptance criteria er oppfylt.
* At tester dekker ny eller endret funksjonalitet.
* At eksisterende tester fortsatt fungerer.
* Kodestandard og lesbarhet.
* Unødvendig kompleksitet eller duplisering.
* Dokumentasjonen er gjennomgått og oppdatert der det er nødvendig.

## <span style="color:#E2E8F0"><u>Regler</u></span>

* Minst **2 godkjenninger** kreves før merge til `master`.
* Review-kommentarer skal være konkrete og konstruktive.
* Forventet responstid på review-forespørsler er **innen 1 arbeidsdag**.

## <span style="color:#E2E8F0"><u>Review-resultat</u></span>

En reviewer kan:

* **Approve** – endringen kan merges.
* **Request changes** – endringer må gjøres før merge.
* **Comment** – kommentar uten krav om endring.

Når endringer er gjort etter en review, skal relevante kommentarer følges opp og PR-en reviewes på nytt ved behov.

</div>

<a id="merge"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">6. Merge</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Regler før merge</u></span>

PR-en kan merges når:

* [ ] Alle acceptance criteria er oppfylt.
* [ ] Relevante tester passerer.
* [ ] Minst to reviewere har godkjent.
* [ ] Alle nødvendige review-kommentarer er håndtert.
* [ ] Det ikke finnes uløste merge conflicts.
* [ ] Branch-en er oppdatert mot `master`.
* [ ] mvn clean verify fullføres uten feil.

## <span style="color:#E2E8F0"><u>Merge-metode</u></span>

Prosjektet bør bruke én fast merge-strategi for å holde historikken konsistent.

Eksempel:

```text
Feature branch
      │
      ▼
Pull Request
      │
      ▼
Code Review
      │
      ▼
Approval + checks
      │
      ▼
Merge → master
```

## <span style="color:#E2E8F0"><u>Etter merge</u></span>

* Kontroller at PR-en er merget til riktig branch.
* Kontroller at tilhørende issue blir lukket.
* Slett feature-branchen dersom den ikke lenger trengs.

</div>

<a id="issue-lukkes"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">7. Issue lukkes</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Når skal issuet lukkes?</u></span>

Et issue lukkes når arbeidet er ferdigstilt og kravene fra issuet er oppfylt.

Dersom PR-en inneholder:

```text
closes #42
```

vil GitHub normalt lukke issue `#42` automatisk når PR-en merges til riktig branch.

## <span style="color:#E2E8F0"><u>Sjekkliste</u></span>

* [ ] Alle acceptance criteria er oppfylt.
* [ ] PR-en er merget.
* [ ] Tester passerer.
* [ ] Eventuell dokumentasjon er oppdatert.
* [ ] Issue har korrekt status.
* [ ] Eventuelle oppfølgingsoppgaver er opprettet som egne issues.

## <span style="color:#E2E8F0"><u>Hvis issuet ikke skal lukkes automatisk</u></span>

Bruk for eksempel:

```text
ref #42
```

i stedet for:

```text
closes #42
```

Da beholdes issuet åpent selv om committen eller PR-en er knyttet til det.

</div>

<a id="ferdig"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">8. Ferdig</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Definition of Done</u></span>

En oppgave regnes som ferdig når:

* [ ] Koden er skrevet og fungerer som forventet.
* [ ] Acceptance criteria er oppfylt.
* [ ] Relevante tester er skrevet eller oppdatert.
* [ ] Alle relevante tester passerer.
* [ ] Koden er reviewet og godkjent.
* [ ] Review-kommentarer er håndtert.
* [ ] PR-en er merget til `master`.
* [ ] Tilhørende issue er lukket.
* [ ] Dokumentasjonen er gjennomgått og oppdatert der det er nødvendig.
* [ ] Feature-branchen er slettet dersom den ikke lenger trengs.

## <span style="color:#E2E8F0"><u>Fullført workflow</u></span>

```text
Issue
  ↓
Branch
  ↓
Commit
  ↓
Pull Request
  ↓
Code Review
  ↓
Approval + Tests
  ↓
Merge
  ↓
Issue Closed
  ↓
Ferdig
```

## <span style="color:#E2E8F0"><u>Hovedregel</u></span>

> **En oppgave er ikke ferdig bare fordi koden fungerer lokalt.**
>
> Den er ferdig når endringen er testet, reviewet, merget og dokumentert ved behov, og det tilhørende issuet er lukket.

</div>

<a id="Kommentarer"></a>

<h1 style="color:#60A5FA; border: none; margin-bottom: 0;">9. Kommentarer</h1>

<hr style="border: none; height: 1px; background: linear-gradient(to right, #60A5FA, transparent); margin: 12px 0 20px 0;">

<div style="background-color: #171717; padding: 20px; border-radius: 8px; border: 1px solid #333; margin-bottom: 30px;">

## <span style="color:#E2E8F0"><u>Kodekommentarer og dokumentasjon</u></span>


Koden skal primært være selvdokumenterende gjennom gode navnevalg på variabler, metoder og klasser. Kommentarer skal brukes bevisst for å øke lesbarheten, ikke for å forklare det koden allerede uttrykker tydelig.

* **Forklar *hvorfor*, ikke *hva*:** Kommentarer skal forklare bakgrunnen for komplekse logiske valg, forretningsregler eller omveier (workarounds), heller enn å gjenta hva koden gjør linje for linje.
* **Javadoc:** Offentlige API-er, sentrale grensesnitt (interfaces) og komplekse offentlige metoder skal ha Javadoc som beskriver hva metoden gjør, argumenter (`@param`), returverdi (`@return`) og eventuelle unntak (`@throws`).
* **Unngå utdatert og utkommentert kode:** Utkommentert kode skal ikke committes til `master`. Ubrukt kode fjernes helt, da tidligere versjoner uansett ligger i Git-historikken.
* **TODO-kommentarer:** `// TODO:` kan brukes underveis i utviklingen på egne bransjer, men skal som hovedregel være resolved eller fylt ut før en Pull Request slås sammen med `master`.

## <span style="color:#E2E8F0"><u>Mal</u></span>

```java
/**
 * [Kort beskrivelse av hva metoden oppnår].
 *
 * @param [paramNavn] [Beskrivelse av parameter]
 * @return [Beskrivelse av returverdi]
 * @throws [ExceptionKlasse] [Når og hvorfor unntaket kastes]
 */
 ```

## <span style="color:#E2E8F0"><u>Eksempel</u></span>

```java
/**
 * Beregner totalprisen for en handlekurv inkludert MVA og eventuell rabatt.
 *
 * Tar hensyn til kampanjekoder dersom kunden er innlogget.
 *
 * @param handlekurv list over varer som skal beregnes
 * @param rabattkode valgfri kampanjekode (kan være null)
 * @return samlet sum avrundet til to dekadiske plasser
 * @throws IllegalArgumentException dersom handlekurven er tom eller null
 */
public double beregnTotalpris(List<Vare> handlekurv, String rabattkode) {
    if (handlekurv == null || handlekurv.isEmpty()) {
        throw new IllegalArgumentException("Handlekurven kan ikke være tom.");
    }

    // Metodeimplementasjon...
}
```

</div>