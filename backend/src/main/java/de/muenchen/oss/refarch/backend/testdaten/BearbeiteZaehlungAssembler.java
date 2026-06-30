package de.muenchen.oss.refarch.backend.testdaten;

import de.muenchen.oss.refarch.backend.dave.client.dto.BearbeiteKnotenarmDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.BearbeiteLaengsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.BearbeiteQuerungsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.BearbeiteZaehlungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.GeoPointDTO;
import de.muenchen.oss.refarch.backend.dave.client.enums.Bewegungsrichtung;
import de.muenchen.oss.refarch.backend.dave.client.enums.Fahrzeug;
import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import de.muenchen.oss.refarch.backend.dave.client.enums.Status;
import de.muenchen.oss.refarch.backend.testdaten.api.KnotenarmDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlungConfigDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Builds the {@link BearbeiteZaehlungDTO} sent to {@code /zaehlung/save} from the UI configuration
 * and the selected relations. The Zählung is created with exactly the selected relations so the
 * later ACCOMPLISHED plausibility check (intervals == intervalCount * relations) is satisfiable.
 */
@Component
public class BearbeiteZaehlungAssembler {

    private static final int DEFAULT_INTERVALL_MINUTEN = 15;

    /**
     * @param idByOptionKey UUID to assign to each relation, keyed by
     *            {@link VerkehrsbeziehungOptionDTO#key()}.
     *            The DAVe backend keeps relation ids we supply (it only generates one when the id is
     *            empty),
     *            so reusing these ids in the subsequent saveExternal call avoids a re-read round-trip.
     */
    public BearbeiteZaehlungDTO baue(final ZaehlungConfigDTO config, final List<VerkehrsbeziehungOptionDTO> beziehungen,
            final Map<String, String> idByOptionKey) {
        final BearbeiteZaehlungDTO dto = new BearbeiteZaehlungDTO();
        dto.setDatum(config.datum());
        dto.setZaehlart(config.zaehlart());
        dto.setZaehldauer(config.zaehldauer());
        dto.setZaehlIntervall(config.zaehlIntervall() == null ? DEFAULT_INTERVALL_MINUTEN : config.zaehlIntervall());
        dto.setKreisverkehr(config.kreisverkehr());
        dto.setSonderzaehlung(config.sonderzaehlung());
        dto.setProjektNummer(config.projektNummer());
        dto.setProjektName(config.projektName());
        dto.setKreuzungsname(config.kreuzungsname());
        dto.setKommentar(config.kommentar());
        dto.setWetter(config.wetter());
        dto.setTagesTyp(config.tagesTyp());
        dto.setDienstleisterkennung(config.dienstleisterkennung());
        dto.setStatus(Status.CREATED.name());
        dto.setQuelle("Testdaten-Generator");

        if (config.lat() != null && config.lng() != null) {
            dto.setLat(config.lat());
            dto.setLng(config.lng());
            dto.setPunkt(new GeoPointDTO(config.lat(), config.lng()));
        }

        dto.setKategorien(mapKategorien(config.kategorien()));
        dto.setKnotenarme(mapKnotenarme(config.knotenarme()));

        final List<BearbeiteVerkehrsbeziehungDTO> verkehrsbeziehungen = new ArrayList<>();
        final List<BearbeiteLaengsverkehrDTO> laengsverkehr = new ArrayList<>();
        final List<BearbeiteQuerungsverkehrDTO> querungsverkehr = new ArrayList<>();
        for (final VerkehrsbeziehungOptionDTO option : beziehungen) {
            final String id = idByOptionKey.get(option.key());
            switch (option.typ()) {
            case VERKEHRSBEZIEHUNG -> {
                final BearbeiteVerkehrsbeziehungDTO vb = toVerkehrsbeziehung(option);
                vb.setId(id);
                verkehrsbeziehungen.add(vb);
            }
            case LAENGSVERKEHR -> {
                final BearbeiteLaengsverkehrDTO lv = toLaengsverkehr(option);
                lv.setId(id);
                laengsverkehr.add(lv);
            }
            case QUERUNGSVERKEHR -> {
                final BearbeiteQuerungsverkehrDTO qv = toQuerungsverkehr(option);
                qv.setId(id);
                querungsverkehr.add(qv);
            }
            }
        }
        dto.setVerkehrsbeziehungen(verkehrsbeziehungen);
        dto.setLaengsverkehr(laengsverkehr);
        dto.setQuerungsverkehr(querungsverkehr);
        return dto;
    }

    private BearbeiteVerkehrsbeziehungDTO toVerkehrsbeziehung(final VerkehrsbeziehungOptionDTO option) {
        final BearbeiteVerkehrsbeziehungDTO vb = new BearbeiteVerkehrsbeziehungDTO();
        final boolean kreisverkehr = option.hinein() != null || option.heraus() != null || option.vorbei() != null;
        if (kreisverkehr) {
            vb.setIsKreuzung(Boolean.FALSE);
            vb.setKnotenarm(option.knotenarm());
            vb.setHinein(Boolean.TRUE.equals(option.hinein()));
            vb.setHeraus(Boolean.TRUE.equals(option.heraus()));
            vb.setVorbei(Boolean.TRUE.equals(option.vorbei()));
        } else {
            vb.setIsKreuzung(Boolean.TRUE);
            vb.setVon(option.von());
            vb.setNach(option.nach());
            vb.setStrassenseite(toHimmelsrichtung(option.strassenseite()));
        }
        return vb;
    }

    private BearbeiteLaengsverkehrDTO toLaengsverkehr(final VerkehrsbeziehungOptionDTO option) {
        final BearbeiteLaengsverkehrDTO lv = new BearbeiteLaengsverkehrDTO();
        lv.setKnotenarm(option.knotenarm());
        lv.setRichtung(option.richtung() == null ? null : Bewegungsrichtung.valueOf(option.richtung()));
        lv.setStrassenseite(toHimmelsrichtung(option.strassenseite()));
        return lv;
    }

    private BearbeiteQuerungsverkehrDTO toQuerungsverkehr(final VerkehrsbeziehungOptionDTO option) {
        final BearbeiteQuerungsverkehrDTO qv = new BearbeiteQuerungsverkehrDTO();
        qv.setKnotenarm(option.knotenarm());
        qv.setRichtung(toHimmelsrichtung(option.richtung()));
        return qv;
    }

    private List<Fahrzeug> mapKategorien(final List<String> kategorien) {
        if (kategorien == null) {
            return List.of();
        }
        final List<Fahrzeug> result = new ArrayList<>();
        for (final String code : kategorien) {
            result.add(Fahrzeug.valueOf(code));
        }
        return result;
    }

    private List<BearbeiteKnotenarmDTO> mapKnotenarme(final List<KnotenarmDTO> knotenarme) {
        if (knotenarme == null) {
            return List.of();
        }
        final List<BearbeiteKnotenarmDTO> result = new ArrayList<>();
        for (final KnotenarmDTO arm : knotenarme) {
            final BearbeiteKnotenarmDTO dto = new BearbeiteKnotenarmDTO();
            dto.setNummer(arm.nummer());
            dto.setStrassenname(arm.strassenname());
            result.add(dto);
        }
        return result;
    }

    private Himmelsrichtung toHimmelsrichtung(final String value) {
        return value == null || value.isBlank() ? null : Himmelsrichtung.valueOf(value);
    }
}
