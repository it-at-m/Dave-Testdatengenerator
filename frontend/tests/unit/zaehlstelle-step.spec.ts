import { createTestingPinia } from "@pinia/testing";
import { mount } from "@vue/test-utils";
import { beforeAll, describe, expect, test, vi } from "vitest";
import { nextTick } from "vue";

import i18n from "@/plugins/i18n";
import vuetify from "@/plugins/vuetify";
import IndexView from "@/routes/index.vue";
import { useTestdatenStore } from "@/stores/testdaten";

// Mount the wizard fully offline: stub the API client so no dave-backend is required.
vi.mock("@/api/testdaten-client", () => ({
  getOptions: vi.fn().mockResolvedValue({
    zaehlarten: [{ code: "N", label: "Standardzählung" }],
    zaehldauern: [
      { code: "DAUER_2_X_4_STUNDEN", label: "2x4h", anzahlZeitintervalle: 32 },
    ],
    fahrzeuge: [{ code: "PKW", label: "Pkw" }],
    himmelsrichtungen: [],
    statusWerte: ["ACCOMPLISHED"],
  }),
  suggestZaehlstellen: vi.fn().mockResolvedValue([]),
  getZaehlstelle: vi.fn(),
  getVerkehrsbeziehungen: vi.fn().mockResolvedValue([]),
  generateCsv: vi.fn(),
  importTestdaten: vi.fn(),
}));

beforeAll(() => {
  // Vuetify touches these browser APIs that jsdom does not implement.
  globalThis.ResizeObserver = class {
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
  };
  globalThis.matchMedia =
    globalThis.matchMedia ??
    (() =>
      ({
        matches: false,
        media: "",
        onchange: null,
        addEventListener: () => {},
        removeEventListener: () => {},
        addListener: () => {},
        removeListener: () => {},
        dispatchEvent: () => false,
      }) as unknown as MediaQueryList);
});

function mountView() {
  return mount(IndexView, {
    global: {
      plugins: [
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
        i18n,
        vuetify,
      ],
    },
  });
}

function weiterButton(wrapper: ReturnType<typeof mountView>) {
  return wrapper
    .findAllComponents({ name: "VBtn" })
    .find((b) => b.text().trim() === "Weiter");
}

describe("Step 1 – Zählstelle (offline / ohne dave-backend)", () => {
  //test("rendert die manuelle Eingabe und übersetzte Texte (keine Roh-i18n-Keys)", async () => {
  //  const wrapper = mountView();
  //  await nextTick();
//
//    // Translated hint is rendered, proving the new i18n keys resolve.
//    expect(wrapper.text()).toContain("Die Suche im DAVe-Backend ist optional");
//    expect(wrapper.text()).toContain("Zählstelle manuell eingeben");
//    // No raw translation keys leak into the UI.
//    expect(wrapper.text()).not.toContain("views.testdaten.zaehlstelle");
//  });

  test("'Weiter' ist ohne Zählstellennummer gesperrt und ohne Zählstellen-ID frei, sobald nur die Nummer gesetzt ist", async () => {
    const wrapper = mountView();
    await nextTick();

    // No Nummer yet -> blocked.
    expect(weiterButton(wrapper)?.props("disabled")).toBe(true);

    // Manual Nummer only, no zaehlstelleId (the dave-backend-sourced field) -> enabled.
    const store = useTestdatenStore();
    store.config.zaehlstelleNummer = "12345";
    await nextTick();

    expect(store.config.zaehlstelleId).toBe("");
    expect(weiterButton(wrapper)?.props("disabled")).toBe(false);
  });
});
