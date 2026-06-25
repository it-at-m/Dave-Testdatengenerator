import type {
  CsvDatei,
  ImportResult,
  Knotenarm,
  OptionsResponse,
  VerkehrsbeziehungOption,
  ZaehlstelleSuggest,
  ZaehlungConfig,
} from "@/types/testdaten";

import { defineStore } from "pinia";
import { computed, ref, watch } from "vue";

import {
  generateCsv,
  getOptions,
  getVerkehrsbeziehungen,
  getZaehlstelle,
  importTestdaten,
  suggestZaehlstellen,
} from "@/api/testdaten-client";
import { ApiError } from "@/api/ApiError";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";

/**
 * Zählarten, bei denen ausschließlich Fuß- und Radverkehr je Straßenseite bzw. Querung erfasst wird.
 * Hier werden standardmäßig nur Rad und Fuß vorbelegt; sonst alle Fahrzeugklassen außer Fuß.
 * (Vgl. examples/testdata im dave-Repository.)
 */
const FUSS_RAD_ZAEHLARTEN = ["QU", "FJS", "QJS"];

function defaultKategorienFor(zaehlart: string): string[] {
  return FUSS_RAD_ZAEHLARTEN.includes(zaehlart)
    ? ["RAD", "FUSS"]
    : ["PKW", "LKW", "LZ", "BUS", "KRAD", "RAD"];
}

/**
 * Knotenarm-Vorbelegung je Zählart. Bei einer Querung (QU) wird immer über zwei
 * gegenüberliegende Knotenarme gezählt, daher werden zwei (z.B. 2 und 4) vorbelegt.
 * Gegenüberliegende Arme laut Belastungsplan: 1↔3, 2↔4, 5↔7, 6↔8.
 * Sonst genügt ein einzelner Arm; weitere bleiben hinzufügbar.
 */
function defaultKnotenarmeFor(zaehlart: string): Knotenarm[] {
  return zaehlart === "QU"
    ? [
        { nummer: 2, strassenname: "Arm 2" },
        { nummer: 4, strassenname: "Arm 4" },
      ]
    : [{ nummer: 1, strassenname: "Arm 1" }];
}

function defaultConfig(): ZaehlungConfig {
  const zaehlart = "N";
  return {
    zaehlstelleId: "",
    zaehlstelleNummer: "",
    lat: null,
    lng: null,
    datum: new Date().toISOString().slice(0, 10),
    zaehlart,
    zaehldauer: "DAUER_2_X_4_STUNDEN",
    zaehlIntervall: 15,
    kreisverkehr: false,
    sonderzaehlung: false,
    projektNummer: null,
    projektName: null,
    kreuzungsname: null,
    kommentar: "Testdaten",
    wetter: null,
    tagesTyp: null,
    dienstleisterkennung: "testdaten",
    kategorien: defaultKategorienFor(zaehlart),
    knotenarme: defaultKnotenarmeFor(zaehlart),
  };
}

export const useTestdatenStore = defineStore("testdaten", () => {
  const snackbarStore = useSnackbarStore();

  const step = ref(1);
  const loading = ref(false);

  const options = ref<OptionsResponse | null>(null);
  const suggestions = ref<ZaehlstelleSuggest[]>([]);
  const config = ref<ZaehlungConfig>(defaultConfig());
  const relationOptions = ref<VerkehrsbeziehungOption[]>([]);
  const selectedKeys = ref<string[]>([]);
  const csvDateien = ref<CsvDatei[]>([]);
  const zielStatus = ref<string>("ACCOMPLISHED");
  const importResult = ref<ImportResult | null>(null);

  const selectedBeziehungen = computed(() =>
    relationOptions.value.filter((o) => selectedKeys.value.includes(o.key))
  );

  // Bei Wechsel der Zählart die Fahrzeugtypen passend vorbelegen:
  // Fuß/Rad-Zählarten (QU, FJS, QJS) -> nur Rad + Fuß, sonst alle außer Fuß.
  // Zusätzlich für QU zwei gegenüberliegende Knotenarme vorbelegen (z.B. 2 und 4).
  // Knotenarme nur beim Wechsel von/zu QU neu setzen, damit selbst eingegebene
  // Straßennamen beim Wechsel zwischen anderen Zählarten erhalten bleiben.
  watch(
    () => config.value.zaehlart,
    (zaehlart, vorher) => {
      config.value.kategorien = defaultKategorienFor(zaehlart);
      if (zaehlart === "QU" || vorher === "QU") {
        config.value.knotenarme = defaultKnotenarmeFor(zaehlart);
      }
    }
  );

  function reportError(error: unknown): void {
    const message =
      error instanceof ApiError
        ? error.message
        : (error as Error)?.message ?? "Unbekannter Fehler.";
    snackbarStore.push({ color: STATUS_INDICATORS.ERROR, text: message });
  }

  async function loadOptions(): Promise<void> {
    if (options.value) {
      return;
    }
    try {
      options.value = await getOptions();
    } catch (error) {
      reportError(error);
    }
  }

  async function search(query: string): Promise<void> {
    if (!query || query.trim().length < 1) {
      suggestions.value = [];
      return;
    }
    loading.value = true;
    try {
      suggestions.value = await suggestZaehlstellen(query.trim());
    } catch (error) {
      reportError(error);
    } finally {
      loading.value = false;
    }
  }

  async function selectZaehlstelle(id: string): Promise<void> {
    loading.value = true;
    try {
      const detail = await getZaehlstelle(id);
      config.value.zaehlstelleId = detail.id;
      config.value.zaehlstelleNummer = detail.nummer;
      config.value.lat = detail.lat;
      config.value.lng = detail.lng;
      config.value.kreuzungsname = detail.name;
    } catch (error) {
      reportError(error);
    } finally {
      loading.value = false;
    }
  }

  async function loadRelations(): Promise<void> {
    loading.value = true;
    try {
      relationOptions.value = await getVerkehrsbeziehungen({
        zaehlart: config.value.zaehlart,
        kreisverkehr: config.value.kreisverkehr,
        knotenarme: config.value.knotenarme,
      });
      // keep only still-valid selections
      const validKeys = new Set(relationOptions.value.map((o) => o.key));
      selectedKeys.value = selectedKeys.value.filter((k) => validKeys.has(k));
    } catch (error) {
      reportError(error);
    } finally {
      loading.value = false;
    }
  }

  async function generate(): Promise<void> {
    loading.value = true;
    try {
      const response = await generateCsv({
        config: config.value,
        ausgewaehlteBeziehungen: selectedBeziehungen.value,
        wertebereiche: null,
      });
      csvDateien.value = response.dateien;
    } catch (error) {
      reportError(error);
    } finally {
      loading.value = false;
    }
  }

  async function doImport(): Promise<void> {
    loading.value = true;
    importResult.value = null;
    try {
      importResult.value = await importTestdaten({
        config: config.value,
        ausgewaehlteBeziehungen: selectedBeziehungen.value,
        dateien: csvDateien.value,
        zielStatus: zielStatus.value,
      });
      if (importResult.value.success) {
        snackbarStore.push({
          color: STATUS_INDICATORS.SUCCESS,
          text: "Testdaten erfolgreich eingespielt.",
        });
      } else {
        snackbarStore.push({
          color: STATUS_INDICATORS.ERROR,
          text: importResult.value.message,
        });
      }
    } catch (error) {
      reportError(error);
    } finally {
      loading.value = false;
    }
  }

  function reset(): void {
    step.value = 1;
    suggestions.value = [];
    config.value = defaultConfig();
    relationOptions.value = [];
    selectedKeys.value = [];
    csvDateien.value = [];
    zielStatus.value = "ACCOMPLISHED";
    importResult.value = null;
  }

  return {
    step,
    loading,
    options,
    suggestions,
    config,
    relationOptions,
    selectedKeys,
    csvDateien,
    zielStatus,
    importResult,
    selectedBeziehungen,
    loadOptions,
    search,
    selectZaehlstelle,
    loadRelations,
    generate,
    doImport,
    reset,
  };
});
