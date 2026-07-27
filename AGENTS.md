DAVe Testdatengenerator – Leitfaden für Agents

Kurzer, hochsignifikanter Leitfaden, um Fehler zu vermeiden und schnell produktiv zu werden.

Was dieses Repo ist

- Monorepo mit drei Teilen:
  - backend/ Spring Boot 4 (Port 8087 im Profil local)
  - frontend/ Vue 3 + Vite (Port 8088; proxyt /api/backend → 8087)
  - docs/ VitePress-Dokumentation (Deploy via GitHub Pages)
- Zweck: CSV-Testdaten für DAVe generieren und optional in ein laufendes dave-backend importieren. Integration ins dave-backend ist noch nicht verifiziert; Fokus auf CSV-Generierung.

Versionen (nicht raten)

- Backend: Java 21.
- Frontend, Docs: Node >=22 <25 und npm >=10 <12. Node 18/20 bricht Installation/Build.

Lokal starten – Schnellweg

1) Backend (Swagger und inbound no-security via Profil local)
   - Windows: `backend\runLocal.bat`
   - Linux/macOS: `bash backend/runLocal.sh`
   - Equivalent: `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local`
   - dave-backend ist für reine CSV-Erzeugung NICHT erforderlich.

2) Frontend (Dev-Server mit Proxy)
   - `cd frontend && npm ci && npm run dev`
   - Öffnen: http://localhost:8088

Nicht offensichtliche Build-Reihenfolge (OpenAPI + Codegen)

- Frontend Dev (`npm run dev`) nutzt einen handgeschriebenen Fetch-Client und benötigt KEINE generierten OpenAPI-Typen.
- Frontend Build und Lint benötigen generierten Code. Reihenfolge beim Bauen/Linten:
  1. Backend mit Profil `local` starten (aktiviert swagger-ui und api-docs).
  2. Spec erzeugen: `mvn -f backend springdoc-openapi:generate` (schreibt YAML nach backend/api-spec/).
  3. Client generieren: `cd frontend && npm run pre-build` (openapi-generator liest ../backend/api-spec/*.yaml).
  4. Build: `npm run build`.
- `npm run lint` triggert ebenfalls `pre-build`; es schlägt fehl, wenn die Spec aus Schritt 2 fehlt.

Backend Linting/Checks

- Strenge Code-Quality-Plugins sind aktiv (Spotless, SpotBugs, …). Für schnelle lokale Compiles:
  - `cd backend && mvn -DskipTests "-Dspotless.check.skip=true" "-Dpmd.skip=true" "-Dcpd.skip=true" "-Dspotbugs.skip=true" compile`

Typprüfung und Tests

- Frontend Typecheck (findet TS-Fehler):
  - `cd frontend && npx vue-tsc --noEmit -p tsconfig.app.json`
- Frontend Tests: `cd frontend && npm test`
- Backend Tests: `cd backend && mvn test`

API-Einstiegspunkte und Pfade (Verkabelung)

- Backend REST-Basis: `/testdaten` (siehe backend/src/main/java/.../testdaten/TestdatenController.java).
- Frontend baut URLs als `${VITE_BASE_API_PATH || "/api/backend"}/testdaten`.
  - Dev-Server proxyt `/api/backend` auf `http://127.0.0.1:8087` (vite.config.ts).
  - Hinter lokalem API-Gateway `VITE_BASE_API_PATH=/api/backend` beibehalten (Default).

Lokaler API-Gateway-Stack (optional, falls SSO/Gateway benötigt)

- `docker compose -f stack/docker-compose.yml up -d`
- Gateway-Routen:
  - `/api/backend/**` → host.docker.internal:8087
  - `/**` → host.docker.internal:8088
- Frontend Dev-Server erlaubt `host.docker.internal` bereits und setzt nötige Header.

Konfiguration – typische Stolpersteine

- Backend-Profil `local` aktiviert implizit `no-security` (eingehende Auth aus). Ausgehende Calls zum echten dave-backend via `dave.backend.*` in backend/src/main/resources/application[-local].yml konfigurieren.
- Import ins dave-backend erfordert Server-seitig Rolle FACHADMIN. Integration ist noch nicht verifiziert; rechne mit 403, außer das dave-backend läuft ohne Security oder OAuth2 ist korrekt konfiguriert.
- Standard-Ports: Backend 8087, Frontend 8088. Bei Änderungen Proxy/Env anpassen.
- Frontend Node-Engine nicht ändern; CI erzwingt Node 22.

CI/Release – relevante Punkte

- .github/workflows/build.yml baut Backend und Frontend mit Java 21 / Node 22 und erstellt Container-Images nur auf Pushes nach main.
- Frontend-Lint ist in CI aktuell deaktiviert (`run-lint: false`); lokal linten, wenn benötigt.
- Releases manuell via GitHub Actions (release-maven.yml für Backend JAR/Image, release-npm.yml für Frontend/Image).

Wo ist was

- Backend API/Controller und CSV-Logik: backend/src/main/java/de/muenchen/oss/refarch/backend/testdaten/**
- Frontend API-Client (Dev): frontend/src/api/testdaten-client.ts
- OpenAPI-Generator-Konfiguration: frontend/openapitools.json
- Vite-Proxy und Dev-Server: frontend/vite.config.ts
- Frontend-Env-Defaults: frontend/.env und frontend/src/constants.ts

Referenz

- Die README.md im Root enthält die vollständigsten Run/Build-Infos. Bei Zweifeln die ausführbaren Konfigurationen (pom.xml, vite.config.ts, openapitools.json) bevorzugen.
