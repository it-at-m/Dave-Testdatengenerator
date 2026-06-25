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

  test("Normalzählung belegt alle Fahrzeugklassen außer Fuß vor", () => {
    const store = useTestdatenStore();
    expect(store.config.kategorien).toEqual([
      "PKW",
      "LKW",
      "LZ",
      "BUS",
      "KRAD",
      "RAD",
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
    ]);
  });
});
