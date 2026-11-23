<template>
  <v-container>
    <h1 class="text-h5 font-weight-bold mb-4">
      {{ t("views.testdaten.title") }}
    </h1>

    <v-stepper
      v-model="store.step"
      :items="stepTitles"
      hide-actions
    >
      <!-- Step 1: Zählstelle -->
      <template #item.1>
        <v-card flat>
<!--          <v-alert-->
<!--            type="info"-->
<!--            variant="tonal"-->
<!--            density="compact"-->
<!--            class="mb-4"-->
<!--          >-->
<!--            {{ t("views.testdaten.zaehlstelle.hinweis") }}-->
<!--          </v-alert>-->

          <!-- Optional: search via dave-backend (requires a running backend) -->
<!--          <div class="text-subtitle-2 mb-1">-->
<!--            {{ t("views.testdaten.zaehlstelle.sucheTitel") }}-->
<!--          </div>-->
<!--          <div class="d-flex ga-2 align-center mb-2">-->
<!--            <v-text-field-->
<!--              v-model="suchbegriff"-->
<!--              :label="t('views.testdaten.zaehlstelle.searchLabel')"-->
<!--              density="comfortable"-->
<!--              hide-details-->
<!--              clearable-->
<!--              @keyup.enter="onSearch"-->
<!--            />-->
<!--            <v-btn-->
<!--              color="primary"-->
<!--              :loading="store.loading"-->
<!--              @click="onSearch"-->
<!--            >-->
<!--              {{ t("common.actions.search") }}-->
<!--            </v-btn>-->
<!--          </div>-->

<!--          <v-list-->
<!--            v-if="store.suggestions.length"-->
<!--            density="compact"-->
<!--            border-->
<!--            rounded-->
<!--          >-->
<!--            <v-list-item-->
<!--              v-for="s in store.suggestions"-->
<!--              :key="s.id"-->
<!--              :title="s.text"-->
<!--              :active="store.config.zaehlstelleId === s.id"-->
<!--              @click="store.selectZaehlstelle(s.id)"-->
<!--            />-->
<!--          </v-list>-->
<!--          <v-alert-->
<!--            v-else-if="sucheAusgefuehrt"-->
<!--            type="info"-->
<!--            variant="tonal"-->
<!--            density="compact"-->
<!--          >-->
<!--            {{ t("views.testdaten.zaehlstelle.noResults") }}-->
<!--          </v-alert>-->

<!--          <v-divider class="my-4" />-->

          <!-- Manual entry: works fully offline up to CSV generation -->
          <div class="text-subtitle-2 mb-2">
            {{ t("views.testdaten.zaehlstelle.manuellTitel") }}
          </div>
          <v-row dense>
            <v-col
              cols="12"
              md="6"
            >
              <v-text-field
                v-model="store.config.zaehlstelleNummer"
                :label="t('views.testdaten.zaehlstelle.nummer')"
                :hint="t('views.testdaten.zaehlstelle.nummerHint')"
                persistent-hint
                clearable
              />
            </v-col>
<!--            <v-col-->
<!--              cols="12"-->
<!--              md="6"-->
<!--            >-->
<!--              <v-text-field-->
<!--                v-model="store.config.kreuzungsname"-->
<!--                :label="t('views.testdaten.zaehlstelle.name')"-->
<!--              />-->
<!--            </v-col>-->
          </v-row>

<!--          <v-expansion-panels class="mt-2">-->
<!--            <v-expansion-panel-->
<!--              :title="t('views.testdaten.zaehlstelle.importFelder')"-->
<!--            >-->
<!--              <v-expansion-panel-text>-->
<!--                <v-row dense>-->
<!--                  <v-col cols="12">-->
<!--                    <v-text-field-->
<!--                      v-model="store.config.zaehlstelleId"-->
<!--                      :label="t('views.testdaten.zaehlstelle.id')"-->
<!--                      density="compact"-->
<!--                    />-->
<!--                  </v-col>-->
<!--                  <v-col-->
<!--                    cols="12"-->
<!--                    md="6"-->
<!--                  >-->
<!--                    <v-text-field-->
<!--                      v-model.number="store.config.lat"-->
<!--                      type="number"-->
<!--                      :label="t('views.testdaten.zaehlstelle.lat')"-->
<!--                      density="compact"-->
<!--                    />-->
<!--                  </v-col>-->
<!--                  <v-col-->
<!--                    cols="12"-->
<!--                    md="6"-->
<!--                  >-->
<!--                    <v-text-field-->
<!--                      v-model.number="store.config.lng"-->
<!--                      type="number"-->
<!--                      :label="t('views.testdaten.zaehlstelle.lng')"-->
<!--                      density="compact"-->
<!--                    />-->
<!--                  </v-col>-->
<!--                </v-row>-->
<!--              </v-expansion-panel-text>-->
<!--            </v-expansion-panel>-->
<!--          </v-expansion-panels>-->

          <div class="d-flex justify-end mt-4">
            <v-btn
              color="primary"
              :disabled="!zaehlstelleNummerVorhanden"
              @click="store.step = 2"
            >
              {{ t("common.actions.next") }}
            </v-btn>
          </div>
        </v-card>
      </template>

      <!-- Step 2: Konfiguration -->
      <template #item.2>
        <v-card flat>
          <v-row>
            <v-col
              cols="12"
              md="6"
            >
              <v-select
                v-model="store.config.zaehlart"
                :items="store.options?.zaehlarten ?? []"
                item-title="label"
                item-value="code"
                :label="t('views.testdaten.konfiguration.zaehlart')"
              />
            </v-col>
            <v-col
              cols="12"
              md="6"
            >
              <v-select
                v-model="store.config.zaehldauer"
                :items="store.options?.zaehldauern ?? []"
                item-title="label"
                item-value="code"
                :label="t('views.testdaten.konfiguration.zaehldauer')"
                :hint="intervallHint"
                persistent-hint
              />
            </v-col>
            <v-col
              cols="12"
              md="6"
            >
              <v-text-field
                v-model="store.config.datum"
                type="date"
                :label="t('views.testdaten.konfiguration.datum')"
              />
            </v-col>
            <v-col
              cols="12"
              md="6"
            >
              <v-select
                v-model="store.config.kategorien"
                :items="store.options?.fahrzeuge ?? []"
                item-title="label"
                item-value="code"
                :label="t('views.testdaten.konfiguration.kategorien')"
                multiple
                chips
              />
            </v-col>
            <v-col
              cols="12"
              md="6"
            >
              <v-switch
                v-model="store.config.kreisverkehr"
                color="primary"
                :label="t('views.testdaten.konfiguration.kreisverkehr')"
              />
            </v-col>
            <v-col
              cols="12"
              md="6"
            >
              <v-switch
                v-model="store.config.sonderzaehlung"
                color="primary"
                :label="t('views.testdaten.konfiguration.sonderzaehlung')"
              />
            </v-col>
<!--            <v-col-->
<!--              cols="12"-->
<!--              md="6"-->
<!--            >-->
<!--              <v-text-field-->
<!--                v-model="store.config.kreuzungsname"-->
<!--                :label="t('views.testdaten.konfiguration.kreuzungsname')"-->
<!--              />-->
<!--            </v-col>-->
<!--            <v-col-->
<!--              cols="12"-->
<!--              md="6"-->
<!--            >-->
<!--              <v-text-field-->
<!--                v-model="store.config.dienstleisterkennung"-->
<!--                :label="t('views.testdaten.konfiguration.dienstleisterkennung')"-->
<!--              />-->
<!--            </v-col>-->
<!--            <v-col-->
<!--              cols="12"-->
<!--              md="6"-->
<!--            >-->
<!--              <v-text-field-->
<!--                v-model="store.config.projektNummer"-->
<!--                :label="t('views.testdaten.konfiguration.projektNummer')"-->
<!--              />-->
<!--            </v-col>-->
<!--            <v-col-->
<!--              cols="12"-->
<!--              md="6"-->
<!--            >-->
<!--              <v-text-field-->
<!--                v-model="store.config.projektName"-->
<!--                :label="t('views.testdaten.konfiguration.projektName')"-->
<!--              />-->
<!--            </v-col>-->
<!--            <v-col cols="12">-->
<!--              <v-text-field-->
<!--                v-model="store.config.kommentar"-->
<!--                :label="t('views.testdaten.konfiguration.kommentar')"-->
<!--              />-->
<!--            </v-col>-->
          </v-row>

          <v-divider class="my-3" />

          <div class="d-flex align-center justify-space-between mb-2">
            <span class="text-subtitle-1">{{
              t("views.testdaten.konfiguration.knotenarme")
            }}</span>
            <v-btn
              size="small"
              variant="tonal"
              @click="addKnotenarm"
            >
              {{ t("views.testdaten.konfiguration.knotenarmHinzufuegen") }}
            </v-btn>
          </div>
          <v-row
            v-for="(arm, i) in store.config.knotenarme"
            :key="i"
            dense
            align="center"
          >
            <v-col cols="3">
              <v-text-field
                v-model.number="arm.nummer"
                type="number"
                :label="t('views.testdaten.konfiguration.armNummer')"
                density="compact"
              />
            </v-col>
            <v-col cols="7">
              <v-text-field
                v-model="arm.strassenname"
                :label="t('views.testdaten.konfiguration.armStrasse')"
                density="compact"
              />
            </v-col>
            <v-col cols="2">
              <v-btn
                icon="$delete"
                variant="text"
                size="small"
                @click="removeKnotenarm(i)"
              />
            </v-col>
          </v-row>

          <div class="d-flex justify-space-between mt-4">
            <v-btn
              variant="text"
              @click="store.step = 1"
            >
              {{ t("common.actions.back") }}
            </v-btn>
            <v-btn
              color="primary"
              @click="goToBeziehungen"
            >
              {{ t("common.actions.next") }}
            </v-btn>
          </div>
        </v-card>
      </template>

      <!-- Step 3: Verkehrsbeziehungen -->
      <template #item.3>
        <v-card flat>
          <p class="mb-2">{{ t("views.testdaten.beziehungen.intro") }}</p>
          <div class="d-flex align-center justify-space-between mb-2">
            <v-checkbox
              :model-value="alleGewaehlt"
              :indeterminate="einigeGewaehlt"
              :label="t('views.testdaten.beziehungen.alleWaehlen')"
              hide-details
              density="compact"
              @update:model-value="toggleAll"
            />
            <span class="text-caption">{{
              t("views.testdaten.beziehungen.anzahlGewaehlt", {
                count: store.selectedKeys.length,
              })
            }}</span>
          </div>
          <v-alert
            v-if="!store.relationOptions.length"
            type="info"
            variant="tonal"
            density="compact"
          >
            {{ t("views.testdaten.beziehungen.keineGewaehlt") }}
          </v-alert>
          <v-list
            v-else
            density="compact"
            class="border rounded"
            max-height="360"
          >
            <v-list-item
              v-for="o in store.relationOptions"
              :key="o.key"
            >
              <v-checkbox
                v-model="store.selectedKeys"
                :value="o.key"
                :label="o.label"
                hide-details
                density="compact"
              />
            </v-list-item>
          </v-list>

          <div class="d-flex justify-space-between mt-4">
            <v-btn
              variant="text"
              @click="store.step = 2"
            >
              {{ t("common.actions.back") }}
            </v-btn>
            <v-btn
              color="primary"
              :disabled="!store.selectedKeys.length"
              @click="store.step = 4"
            >
              {{ t("common.actions.next") }}
            </v-btn>
          </div>
        </v-card>
      </template>

      <!-- Step 4: CSV -->
      <template #item.4>
        <v-card flat>
          <div class="text-subtitle-1 mb-1">
            {{ t("views.testdaten.csv.datengenerierung.titel") }}
          </div>
          <p class="text-caption mb-2">
            {{ t("views.testdaten.csv.datengenerierung.intro") }}
          </p>
          <v-row
            v-for="eintrag in generierbareEintraege"
            :key="eintrag.code"
            dense
            align="center"
          >
            <v-col
              cols="12"
              md="3"
            >
              <span class="text-body-2">{{ fahrzeugLabel(eintrag.code) }}</span>
            </v-col>
            <v-col
              cols="8"
              md="6"
            >
              <v-select
                v-model="eintrag.gen.modus"
                :items="generierungModi"
                :label="t('views.testdaten.csv.datengenerierung.modus')"
                :hint="
                  t(
                    `views.testdaten.csv.datengenerierung.hint.${eintrag.gen.modus}`
                  )
                "
                persistent-hint
                density="compact"
              />
            </v-col>
            <v-col
              cols="4"
              md="3"
            >
              <v-text-field
                v-model.number="eintrag.gen.wert"
                type="number"
                min="0"
                :label="t('views.testdaten.csv.datengenerierung.wert')"
                density="compact"
              />
            </v-col>
          </v-row>

          <v-divider class="my-3" />

          <div class="d-flex ga-2 mb-3">
            <v-btn
              color="primary"
              :loading="store.loading"
              @click="store.generate"
            >
              {{ t("views.testdaten.csv.generieren") }}
            </v-btn>
            <v-btn
              v-if="store.csvDateien.length"
              variant="tonal"
              @click="downloadAlle"
            >
              {{ t("views.testdaten.csv.downloadAlle") }}
            </v-btn>
          </div>

          <v-alert
            v-if="!store.csvDateien.length"
            type="info"
            variant="tonal"
            density="compact"
          >
            {{ t("views.testdaten.csv.keine") }}
          </v-alert>

          <v-expansion-panels
            v-else
            multiple
          >
            <v-expansion-panel
              v-for="d in store.csvDateien"
              :key="d.filename"
            >
              <v-expansion-panel-title>
                {{ d.filename }}
              </v-expansion-panel-title>
              <v-expansion-panel-text>
                <v-btn
                  size="small"
                  variant="tonal"
                  class="mb-2"
                  @click="download(d)"
                >
                  {{ t("views.testdaten.csv.download") }}
                </v-btn>
                <v-textarea
                  :model-value="d.content"
                  readonly
                  rows="10"
                  class="text-mono"
                />
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>

          <div class="d-flex justify-space-between mt-4">
            <v-btn
              variant="text"
              @click="store.step = 3"
            >
              {{ t("common.actions.back") }}
            </v-btn>
<!--            <v-btn-->
<!--              color="primary"-->
<!--              :disabled="!store.csvDateien.length"-->
<!--              @click="store.step = 5"-->
<!--            >-->
<!--              {{ t("common.actions.next") }}-->
<!--            </v-btn>-->
          </div>
        </v-card>
      </template>

<!--      &lt;!&ndash; Step 5: Import &ndash;&gt;-->
<!--      <template #item.5>-->
<!--        <v-card flat>-->
<!--          <v-alert-->
<!--            type="info"-->
<!--            variant="tonal"-->
<!--            density="compact"-->
<!--            class="mb-3"-->
<!--          >-->
<!--            {{ t("views.testdaten.import.hinweis") }}-->
<!--          </v-alert>-->
<!--          <v-alert-->
<!--            v-if="!hatZaehlstelleId"-->
<!--            type="warning"-->
<!--            variant="tonal"-->
<!--            density="compact"-->
<!--            class="mb-3"-->
<!--          >-->
<!--            {{ t("views.testdaten.import.fehlendeId") }}-->
<!--          </v-alert>-->

<!--          <v-select-->
<!--            v-model="store.zielStatus"-->
<!--            :items="store.options?.statusWerte ?? []"-->
<!--            :label="t('views.testdaten.import.zielStatus')"-->
<!--            style="max-width: 320px"-->
<!--          />-->
<!--          <v-btn-->
<!--            color="primary"-->
<!--            :loading="store.loading"-->
<!--            :disabled="!hatZaehlstelleId"-->
<!--            class="mb-4"-->
<!--            @click="store.doImport"-->
<!--          >-->
<!--            {{ t("views.testdaten.import.starten") }}-->
<!--          </v-btn>-->

<!--          <v-alert-->
<!--            v-if="store.importResult"-->
<!--            :type="store.importResult.success ? 'success' : 'error'"-->
<!--            variant="tonal"-->
<!--            class="mb-3"-->
<!--          >-->
<!--            <div>-->
<!--              {{-->
<!--                store.importResult.success-->
<!--                  ? t("views.testdaten.import.erfolg")-->
<!--                  : t("views.testdaten.import.fehler")-->
<!--              }}-->
<!--            </div>-->
<!--            <div v-if="store.importResult.zaehlungId">-->
<!--              {{ t("views.testdaten.import.zaehlungId") }}:-->
<!--              {{ store.importResult.zaehlungId }}-->
<!--            </div>-->
<!--          </v-alert>-->

<!--          <template v-if="store.importResult?.steps?.length">-->
<!--            <div class="text-subtitle-2">-->
<!--              {{ t("views.testdaten.import.schritte") }}-->
<!--            </div>-->
<!--            <v-list density="compact">-->
<!--              <v-list-item-->
<!--                v-for="(s, i) in store.importResult.steps"-->
<!--                :key="i"-->
<!--                :title="s"-->
<!--              />-->
<!--            </v-list>-->
<!--          </template>-->

<!--          <div class="d-flex justify-space-between mt-4">-->
<!--            <v-btn-->
<!--              variant="text"-->
<!--              @click="store.step = 4"-->
<!--            >-->
<!--              {{ t("common.actions.back") }}-->
<!--            </v-btn>-->
<!--            <v-btn-->
<!--              variant="tonal"-->
<!--              @click="store.reset"-->
<!--            >-->
<!--              {{ t("common.actions.reset") }}-->
<!--            </v-btn>-->
<!--          </div>-->
<!--        </v-card>-->
<!--      </template>-->
    </v-stepper>
  </v-container>
</template>

<script setup lang="ts">
import type { CsvDatei, FahrzeugGenerierung } from "@/types/testdaten";

import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

import { useTestdatenStore } from "@/stores/testdaten";

const { t } = useI18n();
const store = useTestdatenStore();

const suchbegriff = ref("");
const sucheAusgefuehrt = ref(false);

const stepTitles = computed(() => [
  t("views.testdaten.steps.zaehlstelle"),
  t("views.testdaten.steps.konfiguration"),
  t("views.testdaten.steps.beziehungen"),
  t("views.testdaten.steps.csv"),
//  t("views.testdaten.steps.import"),
]);

const VERTEILUNGSMODI = [
  "KONSTANT",
  "AUFSTEIGEND",
  "REALISTISCH",
  "ZUFALL",
] as const;

const generierungModi = computed(() =>
  VERTEILUNGSMODI.map((modus) => ({
    value: modus,
    title: t(`views.testdaten.csv.datengenerierung.modi.${modus}`),
  }))
);

// Nur Fahrzeugklassen mit einer Generierungseinstellung anbieten (die Aggregat-
// kategorien wie Kfz/Schwerverkehr werden vom Generator ohnehin nicht befüllt).
const generierbareEintraege = computed(() =>
  store.config.kategorien
    .map((code) => ({ code, gen: store.datengenerierung.proFahrzeug[code] }))
    .filter(
      (e): e is { code: string; gen: FahrzeugGenerierung } => e.gen != null
    )
);

function fahrzeugLabel(code: string): string {
  return store.options?.fahrzeuge.find((f) => f.code === code)?.label ?? code;
}

const intervallHint = computed(() => {
  const info = store.options?.zaehldauern.find(
    (z) => z.code === store.config.zaehldauer
  );
  return info
    ? t("views.testdaten.konfiguration.intervalle", {
        count: info.anzahlZeitintervalle,
      })
    : "";
});

const zaehlstelleNummerVorhanden = computed(
  () => (store.config.zaehlstelleNummer ?? "").trim().length > 0
);
const hatZaehlstelleId = computed(
  () => (store.config.zaehlstelleId ?? "").trim().length > 0
);

const alleGewaehlt = computed(
  () =>
    store.relationOptions.length > 0 &&
    store.selectedKeys.length === store.relationOptions.length
);
const einigeGewaehlt = computed(
  () => store.selectedKeys.length > 0 && !alleGewaehlt.value
);

async function onSearch(): Promise<void> {
  await store.search(suchbegriff.value);
  sucheAusgefuehrt.value = true;
}

function goToBeziehungen(): void {
  store.step = 3;
  store.loadRelations();
}

function addKnotenarm(): void {
  const next =
    store.config.knotenarme.reduce((m, a) => Math.max(m, a.nummer), 0) + 1;
  store.config.knotenarme.push({ nummer: next, strassenname: "Arm " + next });
}

function removeKnotenarm(index: number): void {
  store.config.knotenarme.splice(index, 1);
}

function toggleAll(value: boolean | null): void {
  store.selectedKeys = value ? store.relationOptions.map((o) => o.key) : [];
}

function download(datei: CsvDatei): void {
  const blob = new Blob([datei.content], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = datei.filename;
  a.click();
  URL.revokeObjectURL(url);
}

function downloadAlle(): void {
  store.csvDateien.forEach(download);
}

onMounted(() => {
  store.loadOptions();
});
</script>

<style scoped>
.text-mono :deep(textarea) {
  font-family: monospace;
  font-size: 0.8rem;
}
</style>
