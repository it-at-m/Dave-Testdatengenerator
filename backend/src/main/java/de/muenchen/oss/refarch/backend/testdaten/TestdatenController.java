package de.muenchen.oss.refarch.backend.testdaten;

import de.muenchen.oss.refarch.backend.testdaten.api.GenerateCsvRequestDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.GenerateCsvResponseDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ImportRequestDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ImportResultDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.OptionsDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungRequestDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlstelleDetailDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlstelleSuggestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API of the test-data generator used by the Vue frontend.
 */
@RestController
@RequestMapping("/testdaten")
@RequiredArgsConstructor
public class TestdatenController {

    private final TestdatenService service;

    /**
     * Returns the selection options (Zählarten, Zähldauern, Fahrzeugtypen, Himmelsrichtungen, Status)
     * for the UI.
     */
    @GetMapping("/options")
    public OptionsDTO getOptions() {
        return service.options();
    }

    /**
     * Searches for Zählstellen matching the given query.
     */
    @GetMapping("/zaehlstellen/suggest")
    public List<ZaehlstelleSuggestDTO> suggestZaehlstellen(@RequestParam("query") final String query) {
        return service.suggestZaehlstellen(query);
    }

    /**
     * Loads the detail data of a Zählstelle.
     */
    @GetMapping("/zaehlstellen/{id}")
    public ZaehlstelleDetailDTO getZaehlstelle(@PathVariable("id") final String id) {
        return service.getZaehlstelle(id);
    }

    /**
     * Computes the selectable movement relations for the given Zählart and node arm layout.
     */
    @PostMapping("/verkehrsbeziehungen")
    public List<VerkehrsbeziehungOptionDTO> verkehrsbeziehungen(@RequestBody final VerkehrsbeziehungRequestDTO request) {
        return service.moeglicheBeziehungen(request);
    }

    /**
     * Generates the CSV test data for the configured Zählung and selected relations (one file per
     * Knotenarm).
     */
    @PostMapping("/csv")
    public GenerateCsvResponseDTO generiereCsv(@RequestBody final GenerateCsvRequestDTO request) {
        return service.generiereCsv(request);
    }

    /**
     * Imports the previously generated CSV data into the DAVe backend and sets the final status.
     */
    @PostMapping("/import")
    public ImportResultDTO importiere(@RequestBody final ImportRequestDTO request) {
        return service.importiere(request);
    }
}
