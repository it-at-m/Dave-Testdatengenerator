/**
 * Types mirroring the dave-testdata-generator backend API.
 */

export interface CodeLabel {
  code: string;
  label: string;
}

export interface ZaehldauerInfo {
  code: string;
  label: string;
  anzahlZeitintervalle: number;
}

export interface OptionsResponse {
  zaehlarten: CodeLabel[];
  zaehldauern: ZaehldauerInfo[];
  fahrzeuge: CodeLabel[];
  himmelsrichtungen: string[];
  statusWerte: string[];
}

export interface ZaehlstelleSuggest {
  id: string;
  text: string;
}

export interface ZaehlstelleDetail {
  id: string;
  nummer: string;
  name: string;
  stadtbezirk: string | null;
  lat: number | null;
  lng: number | null;
  kommentar: string | null;
}

export interface Knotenarm {
  nummer: number;
  strassenname: string;
}

export type RelationTyp =
  | "VERKEHRSBEZIEHUNG"
  | "LAENGSVERKEHR"
  | "QUERUNGSVERKEHR";

export interface VerkehrsbeziehungOption {
  key: string;
  label: string;
  typ: RelationTyp;
  von: number | null;
  nach: number | null;
  knotenarm: number | null;
  hinein: boolean | null;
  heraus: boolean | null;
  vorbei: boolean | null;
  strassenseite: string | null;
  richtung: string | null;
}

export interface VerkehrsbeziehungRequest {
  zaehlart: string;
  kreisverkehr: boolean;
  knotenarme: Knotenarm[];
}

export interface ZaehlungConfig {
  zaehlstelleId: string;
  zaehlstelleNummer: string;
  lat: number | null;
  lng: number | null;
  datum: string | null;
  zaehlart: string;
  zaehldauer: string;
  zaehlIntervall: number | null;
  kreisverkehr: boolean;
  sonderzaehlung: boolean;
  projektNummer: string | null;
  projektName: string | null;
  kreuzungsname: string | null;
  kommentar: string | null;
  wetter: string | null;
  tagesTyp: string | null;
  dienstleisterkennung: string | null;
  kategorien: string[];
  knotenarme: Knotenarm[];
}

export interface CsvDatei {
  knotenarmNummer: number;
  filename: string;
  content: string;
}

export interface GenerateCsvRequest {
  config: ZaehlungConfig;
  ausgewaehlteBeziehungen: VerkehrsbeziehungOption[];
  wertebereiche: null;
}

export interface GenerateCsvResponse {
  dateien: CsvDatei[];
}

export interface ImportRequest {
  config: ZaehlungConfig;
  ausgewaehlteBeziehungen: VerkehrsbeziehungOption[];
  dateien: CsvDatei[];
  zielStatus: string | null;
}

export interface ImportResult {
  success: boolean;
  zaehlungId: string | null;
  finalStatus: string | null;
  message: string;
  steps: string[];
}
