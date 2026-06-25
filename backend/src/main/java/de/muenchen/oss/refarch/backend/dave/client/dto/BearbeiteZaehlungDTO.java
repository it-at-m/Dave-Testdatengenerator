package de.muenchen.oss.refarch.backend.dave.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.muenchen.oss.refarch.backend.dave.client.enums.Fahrzeug;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * Request body for {@code POST /zaehlung/save} (internal creation of a Zählung structure, without
 * time intervals). Mirrors the DAVe backend {@code BearbeiteZaehlungDTO}. The {@code pkwEinheit}
 * field is intentionally omitted: when absent the backend fills in the most recent PKW unit itself.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BearbeiteZaehlungDTO {

    private String id;

    private LocalDate datum;

    private String zaehlart;

    private GeoPointDTO punkt;

    private double lat;

    private double lng;

    private String tagesTyp;

    private String monat;

    private String jahreszeit;

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

    private String schulZeiten;

    private String kommentar;

    private List<String> customSuchwoerter;

    private List<BearbeiteKnotenarmDTO> knotenarme;

    private List<BearbeiteLaengsverkehrDTO> laengsverkehr;

    private List<BearbeiteQuerungsverkehrDTO> querungsverkehr;

    private List<BearbeiteVerkehrsbeziehungDTO> verkehrsbeziehungen;

    private boolean unreadMessagesMobilitaetsreferat;

    private String dienstleisterkennung;

}
