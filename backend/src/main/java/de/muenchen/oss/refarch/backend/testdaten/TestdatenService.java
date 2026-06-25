package de.muenchen.oss.refarch.backend.testdaten;

import de.muenchen.oss.refarch.backend.dave.client.DaveBackendClient;
import de.muenchen.oss.refarch.backend.dave.client.DaveBackendException;
import de.muenchen.oss.refarch.backend.dave.client.dto.BackendIdDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.BearbeiteZaehlungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalZaehlungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.LeseZaehlstelleDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.SucheComplexSuggestsDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.UpdateStatusDTO;
import de.muenchen.oss.refarch.backend.dave.client.enums.Fahrzeug;
import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import de.muenchen.oss.refarch.backend.dave.client.enums.Status;
import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehlart;
import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehldauer;
import de.muenchen.oss.refarch.backend.testdaten.api.CsvDateiDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.GenerateCsvRequestDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.GenerateCsvResponseDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ImportRequestDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ImportResultDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.OptionsDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungRequestDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlstelleDetailDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlstelleSuggestDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlungConfigDTO;
import de.muenchen.oss.refarch.backend.testdaten.csv.CsvGeneratorService;
import de.muenchen.oss.refarch.backend.testdaten.csv.CsvZeitintervallMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the test-data workflow against the DAVe backend:
 * create Zählung → INSTRUCTED → re-read relation ids → upload intervals (saveExternal) → final status.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestdatenService {

    private final DaveBackendClient client;
    private final VerkehrsbeziehungFactory verkehrsbeziehungFactory;
    private final BearbeiteZaehlungAssembler assembler;
    private final ExternalZaehlungAssembler externalAssembler;
    private final CsvGeneratorService csvGenerator;
    private final CsvZeitintervallMapper csvMapper;

    public OptionsDTO options() {
        final List<OptionsDTO.CodeLabel> zaehlarten = Arrays.stream(Zaehlart.values())
                .map(z -> new OptionsDTO.CodeLabel(z.name(), z.getBezeichnung()))
                .toList();
        final List<OptionsDTO.ZaehldauerInfo> zaehldauern = Arrays.stream(Zaehldauer.values())
                .map(z -> new OptionsDTO.ZaehldauerInfo(z.name(), z.getBezeichnung(), z.getAnzahlZeitintervalle()))
                .toList();
        final List<OptionsDTO.CodeLabel> fahrzeuge = Arrays.stream(Fahrzeug.values())
                .map(f -> new OptionsDTO.CodeLabel(f.name(), f.getBezeichnung()))
                .toList();
        final List<String> himmelsrichtungen = Arrays.stream(Himmelsrichtung.values()).map(Enum::name).toList();
        final List<String> statusWerte = Arrays.stream(Status.values()).map(Enum::name).toList();
        return new OptionsDTO(zaehlarten, zaehldauern, fahrzeuge, himmelsrichtungen, statusWerte);
    }

    public List<ZaehlstelleSuggestDTO> suggestZaehlstellen(final String query) {
        final SucheComplexSuggestsDTO result = client.suggestZaehlstellen(query);
        if (result == null || result.getZaehlstellenSuggests() == null) {
            return List.of();
        }
        return result.getZaehlstellenSuggests().stream()
                .map(s -> new ZaehlstelleSuggestDTO(s.getId(), s.getText()))
                .toList();
    }

    public ZaehlstelleDetailDTO getZaehlstelle(final String id) {
        final LeseZaehlstelleDTO z = client.getZaehlstelle(id);
        if (z == null) {
            throw new DaveBackendException("Zählstelle nicht gefunden: " + id);
        }
        return new ZaehlstelleDetailDTO(z.getId(), z.getNummer(), z.getName(), z.getStadtbezirk(),
                z.getLat(), z.getLng(), z.getKommentar());
    }

    public List<VerkehrsbeziehungOptionDTO> moeglicheBeziehungen(final VerkehrsbeziehungRequestDTO request) {
        return verkehrsbeziehungFactory.moeglicheBeziehungen(request.zaehlart(), request.kreisverkehr(), request.knotenarme());
    }

    public GenerateCsvResponseDTO generiereCsv(final GenerateCsvRequestDTO request) {
        final List<CsvDateiDTO> dateien = csvGenerator.generiere(request.config(), request.ausgewaehlteBeziehungen(), request.datengenerierung());
        return new GenerateCsvResponseDTO(dateien);
    }

    public ImportResultDTO importiere(final ImportRequestDTO request) {
        final ZaehlungConfigDTO config = request.config();
        final List<String> steps = new ArrayList<>();
        final String dienstleisterkennung = config.dienstleisterkennung();
        final String zielStatus = request.zielStatus() == null || request.zielStatus().isBlank()
                ? Status.ACCOMPLISHED.name()
                : request.zielStatus();
        try {
            // Assign our own UUIDs per selected relation; the backend keeps supplied ids (see
            // ZaehlstelleIndexService.erstelleZaehlung), so we can reuse them for saveExternal without re-reading.
            final Map<String, String> idByOptionKey = new HashMap<>();
            for (final VerkehrsbeziehungOptionDTO option : request.ausgewaehlteBeziehungen()) {
                idByOptionKey.put(option.key(), UUID.randomUUID().toString());
            }

            // 1. create the Zählung structure (without intervals)
            final BearbeiteZaehlungDTO bearbeiteZaehlung = assembler.baue(config, request.ausgewaehlteBeziehungen(), idByOptionKey);
            final BackendIdDTO created = client.saveZaehlung(bearbeiteZaehlung, config.zaehlstelleId());
            final String zaehlungId = created.getId();
            steps.add("Zählung angelegt (id=" + zaehlungId + ")");

            // 2. instruct the Zählung (and persist the dienstleisterkennung)
            client.updateStatus(new UpdateStatusDTO(zaehlungId, Status.INSTRUCTED.name(), dienstleisterkennung));
            steps.add("Status auf INSTRUCTED gesetzt");

            // 3. build the external upload DTO (reusing the relation ids) and attach the CSV time intervals
            final ExternalZaehlungDTO external = externalAssembler.baue(zaehlungId, config, request.ausgewaehlteBeziehungen(), idByOptionKey);
            csvMapper.mappeZeitintervalle(external, request.dateien(), config.zaehlart(), config.kreisverkehr());
            steps.add("Zähldaten aus CSV übernommen");

            // 4. upload the count data
            client.saveExternal(external);
            steps.add("Zähldaten hochgeladen (saveExternal)");

            // 5. set the final status (ACCOMPLISHED triggers value computation in the backend)
            client.updateStatus(new UpdateStatusDTO(zaehlungId, zielStatus, dienstleisterkennung));
            steps.add("Status auf " + zielStatus + " gesetzt");

            return new ImportResultDTO(true, zaehlungId, zielStatus,
                    "Testdaten erfolgreich eingespielt.", steps);
        } catch (final DaveBackendException ex) {
            log.error("Import der Testdaten fehlgeschlagen", ex);
            steps.add("Fehler: " + ex.getMessage());
            return new ImportResultDTO(false, null, null, ex.getMessage(), steps);
        }
    }
}
