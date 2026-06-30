package de.muenchen.oss.refarch.backend.dave.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.muenchen.oss.refarch.backend.dave.client.enums.Fahrzeug;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * Body for {@code POST /zaehlung/saveExternal} and the elements returned by
 * {@code GET /zaehlung/getZaehlungenForExternal}. Mirrors the DAVe backend
 * {@code ExternalZaehlungDTO}.
 * The movement-relation lists carry the persisted ids needed to attach the uploaded time intervals.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalZaehlungDTO {

    private String id;

    private LocalDate datum;

    private String zaehlart;

    private GeoPointDTO punkt;

    private String projektNummer;

    private String projektName;

    private String kreuzungsname;

    private boolean sonderzaehlung;

    private boolean kreisverkehr;

    private List<Fahrzeug> kategorien;

    private String zaehlsituation;

    private String zaehlsituationErweitert;

    private int zaehlIntervall;

    private String wetter;

    private String status;

    private String quelle;

    private String zaehldauer;

    private String kommentar;

    private List<ExternalKnotenarmDTO> knotenarme;

    private List<ExternalLaengsverkehrDTO> laengsverkehr;

    private List<ExternalQuerungsverkehrDTO> querungsverkehr;

    private List<ExternalVerkehrsbeziehungDTO> verkehrsbeziehungen;

    // Zählstelle
    private String zaehlstelleNummer;

    private String zaehlstelleStadtbezirk;

    private GeoPointDTO zaehlstellePunkt;

    private String zaehlstelleKommentar;

    private boolean unreadMessagesDienstleister;

    private String dienstleisterkennung;

}
