# Zuordnung Knotenarm → Himmelsrichtung / Straßenseite

Diese Notiz dokumentiert, welche Himmelsrichtung bzw. welche beiden gegenüberliegenden
Straßenseiten zu einem Knotenarm gehören. Sie ist die fachliche Grundlage für die
Sonderzählarten **QU** (Querung), **FJS** (Fuß & Rad je Straßenseite) und
**QJS** (Querschnitt je Straßenseite) im DAVe-Testdatengenerator.

## Ergebnis der Recherche

Im **dave-backend** gibt es **keine** explizite Zuordnung von Knotenarm-Nummer zu
Himmelsrichtung. Der Belastungsplan arbeitet nur mit einem `String[8]`-Array, das über
`knotenarm.getNummer() - 1` indiziert wird (z. B.
`BelastungsplanDataQuService`, `BelastungsplanDataFjsService`,
`BelastungsplanDataQjsService`, `BelastungsplanDataDefaultService`,
`ProcessZaehldatenBelastungsplanService`). Welche Position welcher Himmelsrichtung
entspricht, wird erst beim Zeichnen im Frontend festgelegt.

Maßgeblich sind daher zwei Frontend-Quellen:

1. **CSV-Upload-Validierung** im `dave-selfservice-portal` – genau der Validator, den die
   vom Generator erzeugten CSV-Dateien bestehen müssen:
   `frontend/src/components/zaehlung/form/KnotenLageForm.vue`

   ```ts
   // FJS / QJS: Strassenseite je Knotenarm
   isArmnummerAndStrassenseiteInvalid(seite, armNummer, [1, 3], [Strassenseite.W, Strassenseite.O])
   isArmnummerAndStrassenseiteInvalid(seite, armNummer, [2, 4], [Strassenseite.N, Strassenseite.S])
   isArmnummerAndStrassenseiteInvalid(seite, armNummer, [5, 7], [Strassenseite.NW, Strassenseite.SO])
   isArmnummerAndStrassenseiteInvalid(seite, armNummer, [6, 8], [Strassenseite.NO, Strassenseite.SW])
   ```

   Für **QU** ist die Richtung weniger streng: erlaubt sind dort alle acht
   Himmelsrichtungen (`N, O, S, W, NO, SO, NW, SW`) unabhängig vom Arm. Der Generator
   verwendet trotzdem die zur Achse des Arms passenden beiden Richtungen.

2. **Belastungsplan-Darstellung** im `dave-frontend`:
   `frontend/src/components/zaehlstelle/charts/BelastungsplanMethods.ts`

   ```ts
   const rotation = new Map<number, number>([
     [1, 90], [2, 180], [3, 270], [4, 0],      // O, S, W, N
     [5, 135], [6, 225], [7, 315], [8, 45],    // SO, SW, NW, NO
   ]);
   ```

Beide Quellen sind konsistent: Die beiden Straßenseiten eines Arms sind die beiden
Endpunkte der Achse, auf der der Arm liegt.

## Zuordnungstabelle

Die Himmelsrichtungs-Reihenfolge ist `N, NO, O, SO, S, SW, W, NW` (im Uhrzeigersinn).

| Knotenarm | zeigt nach | gegenüberliegende Straßenseiten / Achse | gegenüber |
| --------- | ---------- | --------------------------------------- | --------- |
| 1         | O          | O / W                                   | 3         |
| 2         | S          | N / S                                   | 4         |
| 3         | W          | O / W                                   | 1         |
| 4         | N          | N / S                                   | 2         |
| 5         | SO         | SO / NW                                 | 7         |
| 6         | SW         | NO / SW                                 | 8         |
| 7         | NW         | SO / NW                                 | 5         |
| 8         | NO         | NO / SW                                 | 6         |

Die Arme **1–4** zeigen in die Haupt-, die Arme **5–8** in die kombinierten
Himmelsrichtungen. Gegenüberliegende Arme: **1↔3, 2↔4, 5↔7, 6↔8**.

## Umsetzung im Generator

- `VerkehrsbeziehungFactory.strassenseitenFuerArm(int arm)` liefert die beiden
  Straßenseiten/Richtungen je Arm gemäß obiger Tabelle. Sie wird von FJS
  (Längsverkehr), QU (Querungsverkehr) und QJS (Querschnitt je Straßenseite)
  genutzt; die Korrektur wirkt damit sowohl auf die CSV-Generierung als auch auf
  den direkten Import.
- **QU** braucht immer zwei gegenüberliegende Knotenarme. Im Frontend-Store
  (`stores/testdaten.ts`, `defaultKnotenarmeFor`) werden bei Auswahl der Zählart QU
  zwei gegenüberliegende Arme (2 und 4) vorbelegt.

## Quellen (Stand der Recherche)

- `dave-selfservice-portal/frontend/src/components/zaehlung/form/KnotenLageForm.vue`
- `dave-frontend/frontend/src/components/zaehlstelle/charts/BelastungsplanMethods.ts`
- `dave-backend/.../services/processzaehldaten/BelastungsplanData*Service.java`
  (nur `streets[nummer - 1]`, keine Himmelsrichtungs-Zuordnung)
