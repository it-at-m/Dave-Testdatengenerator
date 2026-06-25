package de.muenchen.oss.refarch.backend.testdaten.api;

/**
 * Optional min/max ranges for the randomly generated counting values per vehicle class. Any null
 * range falls back to a sensible default in the generator.
 */
public record WertebereicheDTO(
        Range pkw,
        Range lkw,
        Range lastzuege,
        Range busse,
        Range kraftraeder,
        Range fahrradfahrer,
        Range fussgaenger) {

    public record Range(Integer min, Integer max) {
    }
}
