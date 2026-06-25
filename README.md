# DAVe Testdaten-Generator

Werkzeug zur Generierung und Einspielung von **Testdaten (Zählungen)** in das
[dave-backend](https://github.com/it-at-m/dave-backend). Aufgebaut auf der
it@M-Referenzarchitektur ([refarch-templates](https://github.com/it-at-m/refarch-templates)):
Vue-3-Frontend + Spring-Boot-Backend (BFF).

## Was macht die Anwendung?

Ein Assistent (Stepper) führt durch:

1. **Zählstelle** wählen. Die Suche im dave-backend ist **optional** – alternativ kann die
   Zählstelle manuell eingegeben werden (nur die Zählstellennummer ist für die CSV-Erzeugung
   nötig; ID/lat/lng werden ausschließlich für das Einspielen gebraucht). Dadurch funktioniert
   der Assistent bis einschließlich CSV-Erzeugung **ohne erreichbares dave-backend**.
2. **Zählung konfigurieren** – Zählart, Zähldauer, Datum, Fahrzeugtypen, Kreisverkehr,
   Knotenarme, Projekt-/Kreuzungsangaben, Dienstleisterkennung.
3. **Verkehrsbeziehungen / Pfeile** auswählen (je nach Zählart automatisch berechnet).
4. **CSV-Testdaten generieren** – im Format des Selfservice-Portals, mit Vorschau und Download.
5. **Einspielen** – die Anwendung fährt am dave-backend den echten Dienstleister-Flow:
   `zaehlung/save` → `updateStatus(INSTRUCTED)` → `saveExternal` (Zähldaten) →
   `updateStatus(<Zielstatus>, Default ACCOMPLISHED)`. Die UUIDs der Verkehrsbeziehungen werden
   schon beim Anlegen selbst vergeben (das Backend behält übergebene IDs bei), daher ist kein
   Zurücklesen via `getZaehlungenForExternal` nötig.

Pro ausgewählter Verkehrsbeziehung wird **genau** `anzahlZeitintervalle` der Zähldauer erzeugt
(2×4h=32, 13h=52, 16h=64, 24h=96), damit die Plausibilitätsprüfung des Backends bei ACCOMPLISHED
erfüllt ist.

## Voraussetzungen

- Java 21+ und Maven (oder Build über die IDE)
- Node `>=24.11 <25` und npm `>=11.6 <12`
- Ein erreichbares **dave-backend** (lokal am einfachsten via `runLocalNoSecurity`) – **nur** für
  die Zählstellensuche und das Einspielen. Für Konfiguration, Verkehrsbeziehungen und
  CSV-Erzeugung wird kein dave-backend benötigt.

## Konfiguration (Backend → dave-backend)

In `backend/src/main/resources/application.yml` bzw. `application-local.yml`:

```yaml
dave:
  backend:
    base-url: http://localhost:8080   # URL des dave-backend
    auth:
      mode: none                      # none | oauth2
      # für oauth2 (Client-Credentials):
      token-uri: ""
      client-id: ""
      client-secret: ""
      scope: ""
```

Das `local`-Profil aktiviert zusätzlich das Profil `no-security` (eingehende Auth deaktiviert),
damit das Frontend das Backend lokal ohne Keycloak ansprechen kann. **Nicht in Produktion verwenden.**

## Starten (lokal)

1. **dave-backend** lokal starten (z. B. `runLocalNoSecurity`), passende `base-url` setzen.
2. **Backend** (Port 8086):
   ```
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   (Während der Entwicklung können die strengen Lints übersprungen werden:
   `-Dspotless.check.skip=true -Dpmd.skip=true -Dcpd.skip=true -Dspotbugs.skip=true`.)
3. **Frontend** (Port 8081, Vite-Proxy `/api/backend` → `http://localhost:8086`):
   ```
   cd frontend
   npm install
   npm run dev
   ```
   Aufruf: <http://localhost:8081>

Hinweis: `npm run dev` benötigt **keinen** generierten OpenAPI-Client – das Frontend nutzt einen
handgeschriebenen fetch-Client (`src/api/testdaten-client.ts`). Für einen Produktions-Build
(`npm run build`) wird – wie im refarch-Template – die OpenAPI-Spec benötigt:
Backend starten und `mvn springdoc-openapi:generate` ausführen, danach `npm run pre-build`.

## Status & Verifikation

Der Code ist vollständig implementiert, wurde aber in der Entstehungsumgebung **noch nicht
kompiliert, typgeprüft oder ausgeführt** (dort waren weder Maven noch Node verfügbar). Vor dem
ersten Einsatz daher bitte diese Gates ausführen:

- Backend kompilieren:
  `mvn -DskipTests -Dspotless.check.skip=true -Dpmd.skip=true -Dcpd.skip=true -Dspotbugs.skip=true compile`
- Frontend typprüfen (nur das deckt TS-Fehler auf – `npm run dev` prüft keine Typen):
  `npx vue-tsc --noEmit -p tsconfig.app.json`

Gegen ein laufendes dave-backend zu prüfen (erst dann belastbar):

- Die Anlege-Endpunkte (`/zaehlung/save`) verlangen serverseitig die Rolle **FACHADMIN**.
  Sicherstellen, dass der gewählte Auth-Modus (`none` gegen `runLocalNoSecurity`, sonst OAuth2)
  am dave-backend tatsächlich Schreibzugriff erlaubt – sonst kommt HTTP 403.
- End-to-End: Zählstelle suchen → konfigurieren → CSV erzeugen → einspielen → im DAVe-Datenportal
  prüfen, dass die Zählung mit Werten erscheint.

## Architektur (Kurz)

```
backend/  de.muenchen.oss.refarch.backend
  dave.client.*        RestClient + gespiegelte dave-backend-DTOs/Enums
  testdaten.csv.*      CSV-Generator, Mapper, Diskriminator, Intervall-Fenster
  testdaten.*          VerkehrsbeziehungFactory, Assembler, TestdatenService, Controller, api.*
frontend/ src
  api/testdaten-client.ts   fetch-Client
  stores/testdaten.ts       Wizard-State (Pinia)
  routes/index.vue          Stepper-UI
  types/testdaten.ts        TS-Typen
```
