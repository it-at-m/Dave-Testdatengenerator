import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, test } from "vitest";
import { nextTick } from "vue";

import { useTestdatenStore } from "@/stores/testdaten";

describe("testdaten store – Vorbelegung", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  test("standardmäßig nur ein Knotenarm", () => {
    const store = useTestdatenStore();
    expect(store.config.knotenarme).toHaveLength(1);
    expect(store.config.knotenarme[0].nummer).toBe(1);
  });

  test("Normalzählung belegt alle Fahrzeugklassen vor", () => {
    const store = useTestdatenStore();
    expect(store.config.kategorien).toEqual([
      "PKW",
      "LKW",
      "LZ",
      "BUS",
      "KRAD",
      "RAD",
      "FUSS",
    ]);
  });

  test.each(["QU", "FJS", "QJS"])(
    "Zählart %s belegt nur Rad und Fuß vor",
    async (zaehlart) => {
      const store = useTestdatenStore();
      store.config.zaehlart = zaehlart;
      await nextTick();
      expect(store.config.kategorien).toEqual(["RAD", "FUSS"]);
    }
  );

  test("QJS belegt zwei gegenüberliegende Knotenarme vor", async () => {
    const store = useTestdatenStore();
    expect(store.config.knotenarme).toHaveLength(1);

    store.config.zaehlart = "QJS";
    await nextTick();

    expect(store.config.knotenarme.map((a) => a.nummer)).toEqual([2, 4]);
  });

  test("Wechsel von QJS zurück stellt einen einzelnen Knotenarm wieder her", async () => {
    const store = useTestdatenStore();
    store.config.zaehlart = "QJS";
    await nextTick();
    expect(store.config.knotenarme).toHaveLength(2);

    store.config.zaehlart = "N";
    await nextTick();
    expect(store.config.knotenarme.map((a) => a.nummer)).toEqual([1]);
  });

  test("Wechsel zwischen Nicht-QU-Zählarten lässt eigene Knotenarme unangetastet", async () => {
    const store = useTestdatenStore();
    store.config.knotenarme = [
      { nummer: 1, strassenname: "Hauptstraße" },
      { nummer: 2, strassenname: "Nebenstraße" },
    ];

    store.config.zaehlart = "FJS";
    await nextTick();

    expect(store.config.knotenarme).toEqual([
      { nummer: 1, strassenname: "Hauptstraße" },
      { nummer: 2, strassenname: "Nebenstraße" },
    ]);
  });

  test("Änderung an der Konfiguration verwirft erzeugte CSV-Dateien", async () => {
    const store = useTestdatenStore();
    store.csvDateien = [
      { knotenarmNummer: 1, filename: "test.csv", content: "x" },
    ];

    store.config.kommentar = "geändert";
    await nextTick();

    expect(store.csvDateien).toHaveLength(0);
  });

  test("Änderung an der Datengenerierung verwirft erzeugte CSV-Dateien", async () => {
    const store = useTestdatenStore();
    store.csvDateien = [
      { knotenarmNummer: 1, filename: "test.csv", content: "x" },
    ];

    store.datengenerierung.proFahrzeug.PKW.modus = "KONSTANT";
    await nextTick();

    expect(store.csvDateien).toHaveLength(0);
  });

  test("Datengenerierung ist standardmäßig zufällig je Fahrzeugtyp", () => {
    const store = useTestdatenStore();
    expect(store.datengenerierung.proFahrzeug.PKW.modus).toBe("ZUFALL");
    expect(store.datengenerierung.proFahrzeug.FUSS.modus).toBe("ZUFALL");
  });

  test("Wechsel zurück auf Normalzählung stellt alle außer Fuß wieder her", async () => {
    const store = useTestdatenStore();
    store.config.zaehlart = "FJS";
    await nextTick();
    expect(store.config.kategorien).toEqual(["RAD", "FUSS"]);

    store.config.zaehlart = "N";
    await nextTick();
    expect(store.config.kategorien).toEqual([
      "PKW",
      "LKW",
      "LZ",
      "BUS",
      "KRAD",
      "RAD",
      "FUSS",
    ]);
  });

  test("Zählart R belegt nur Rad vor", async () => {
    const store = useTestdatenStore();
    store.config.zaehlart = "R";
    await nextTick();
    expect(store.config.kategorien).toEqual(["RAD"]);
  });
});
