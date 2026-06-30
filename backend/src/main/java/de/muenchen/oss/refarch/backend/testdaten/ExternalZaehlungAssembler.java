package de.muenchen.oss.refarch.backend.testdaten;

import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalKnotenarmDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalLaengsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalQuerungsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalVerkehrsbeziehungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalZaehlungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.GeoPointDTO;
import de.muenchen.oss.refarch.backend.dave.client.enums.Bewegungsrichtung;
import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import de.muenchen.oss.refarch.backend.testdaten.api.KnotenarmDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlungConfigDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Builds the {@link ExternalZaehlungDTO} sent to {@code /zaehlung/saveExternal}. The relations
 * reuse
 * the same ids supplied when the Zählung was created (see {@link BearbeiteZaehlungAssembler}); the
 * CSV-derived time intervals are attached afterwards by the mapper.
 */
@Component
public class ExternalZaehlungAssembler {

    private static final int DEFAULT_INTERVALL_MINUTEN = 15;

    public ExternalZaehlungDTO baue(final String zaehlungId, final ZaehlungConfigDTO config,
            final List<VerkehrsbeziehungOptionDTO> beziehungen, final Map<String, String> idByOptionKey) {
        final ExternalZaehlungDTO dto = new ExternalZaehlungDTO();
        dto.setId(zaehlungId);
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
        dto.setQuelle("Testdaten-Generator");
        dto.setDienstleisterkennung(config.dienstleisterkennung());
        if (config.lat() != null && config.lng() != null) {
            dto.setPunkt(new GeoPointDTO(config.lat(), config.lng()));
        }
        dto.setKnotenarme(mapKnotenarme(config.knotenarme()));

        final List<ExternalVerkehrsbeziehungDTO> verkehrsbeziehungen = new ArrayList<>();
        final List<ExternalLaengsverkehrDTO> laengsverkehr = new ArrayList<>();
        final List<ExternalQuerungsverkehrDTO> querungsverkehr = new ArrayList<>();
        for (final VerkehrsbeziehungOptionDTO option : beziehungen) {
            final String id = idByOptionKey.get(option.key());
            switch (option.typ()) {
            case VERKEHRSBEZIEHUNG -> verkehrsbeziehungen.add(toVerkehrsbeziehung(option, id));
            case LAENGSVERKEHR -> laengsverkehr.add(toLaengsverkehr(option, id));
            case QUERUNGSVERKEHR -> querungsverkehr.add(toQuerungsverkehr(option, id));
            }
        }
        dto.setVerkehrsbeziehungen(verkehrsbeziehungen);
        dto.setLaengsverkehr(laengsverkehr);
        dto.setQuerungsverkehr(querungsverkehr);
        return dto;
    }

    private ExternalVerkehrsbeziehungDTO toVerkehrsbeziehung(final VerkehrsbeziehungOptionDTO option, final String id) {
        final ExternalVerkehrsbeziehungDTO vb = new ExternalVerkehrsbeziehungDTO();
        vb.setId(id);
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

    private ExternalLaengsverkehrDTO toLaengsverkehr(final VerkehrsbeziehungOptionDTO option, final String id) {
        final ExternalLaengsverkehrDTO lv = new ExternalLaengsverkehrDTO();
        lv.setId(id);
        lv.setKnotenarm(option.knotenarm());
        lv.setRichtung(option.richtung() == null ? null : Bewegungsrichtung.valueOf(option.richtung()));
        lv.setStrassenseite(toHimmelsrichtung(option.strassenseite()));
        return lv;
    }

    private ExternalQuerungsverkehrDTO toQuerungsverkehr(final VerkehrsbeziehungOptionDTO option, final String id) {
        final ExternalQuerungsverkehrDTO qv = new ExternalQuerungsverkehrDTO();
        qv.setId(id);
        qv.setKnotenarm(option.knotenarm());
        qv.setRichtung(toHimmelsrichtung(option.richtung()));
        return qv;
    }

    private List<ExternalKnotenarmDTO> mapKnotenarme(final List<KnotenarmDTO> knotenarme) {
        if (knotenarme == null) {
            return List.of();
        }
        final List<ExternalKnotenarmDTO> result = new ArrayList<>();
        for (final KnotenarmDTO arm : knotenarme) {
            final ExternalKnotenarmDTO dto = new ExternalKnotenarmDTO();
            dto.setNummer(arm.nummer());
            dto.setStrassenname(arm.strassenname());
            dto.setFilename("");
            result.add(dto);
        }
        return result;
    }

    private Himmelsrichtung toHimmelsrichtung(final String value) {
        return value == null || value.isBlank() ? null : Himmelsrichtung.valueOf(value);
    }
}
