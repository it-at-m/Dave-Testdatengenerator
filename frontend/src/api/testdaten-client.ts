import type {
  GenerateCsvRequest,
  GenerateCsvResponse,
  ImportRequest,
  ImportResult,
  OptionsResponse,
  VerkehrsbeziehungOption,
  VerkehrsbeziehungRequest,
  ZaehlstelleDetail,
  ZaehlstelleSuggest,
} from "@/types/testdaten";

import {
  defaultCatchHandler,
  defaultResponseHandler,
  getConfig,
  postConfig,
} from "@/api/fetch-utils";
import { BASE_API_PATH } from "@/constants";

const BASE = `${BASE_API_PATH ?? "/api/backend"}/testdaten`;

function getJson<T>(url: string, errorMessage: string): Promise<T> {
  return fetch(url, getConfig())
    .catch(defaultCatchHandler)
    .then((response) => {
      defaultResponseHandler(response, errorMessage);
      return response.json() as Promise<T>;
    });
}

function postJson<T>(
  url: string,
  body: unknown,
  errorMessage: string
): Promise<T> {
  return fetch(url, postConfig(body))
    .catch(defaultCatchHandler)
    .then((response) => {
      defaultResponseHandler(response, errorMessage);
      return response.json() as Promise<T>;
    });
}

export function getOptions(): Promise<OptionsResponse> {
  return getJson(
    `${BASE}/options`,
    "Die Auswahloptionen konnten nicht geladen werden."
  );
}

export function suggestZaehlstellen(
  query: string
): Promise<ZaehlstelleSuggest[]> {
  return getJson(
    `${BASE}/zaehlstellen/suggest?query=${encodeURIComponent(query)}`,
    "Die Suche nach Zählstellen ist fehlgeschlagen."
  );
}

export function getZaehlstelle(id: string): Promise<ZaehlstelleDetail> {
  return getJson(
    `${BASE}/zaehlstellen/${encodeURIComponent(id)}`,
    "Die Zählstelle konnte nicht geladen werden."
  );
}

export function getVerkehrsbeziehungen(
  request: VerkehrsbeziehungRequest
): Promise<VerkehrsbeziehungOption[]> {
  return postJson(
    `${BASE}/verkehrsbeziehungen`,
    request,
    "Die Verkehrsbeziehungen konnten nicht ermittelt werden."
  );
}

export function generateCsv(
  request: GenerateCsvRequest
): Promise<GenerateCsvResponse> {
  return postJson(
    `${BASE}/csv`,
    request,
    "Die CSV-Daten konnten nicht erzeugt werden."
  );
}

export function importTestdaten(request: ImportRequest): Promise<ImportResult> {
  return postJson(
    `${BASE}/import`,
    request,
    "Der Import der Testdaten ist fehlgeschlagen."
  );
}
