package de.muenchen.oss.refarch.backend.testdaten.csv;

import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehldauer;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes the list of 15-minute counting intervals that belong to a given {@link Zaehldauer}.
 * The number of intervals must match {@link Zaehldauer#getAnzahlZeitintervalle()} because the DAVe
 * backend verifies {@code intervalCount * relations == storedIntervals} when a Zählung is set to
 * ACCOMPLISHED.
 *
 * <p>
 * Interval {@code nummer} is 1-based over the whole day: interval n starts at {@code (n-1)*15}
 * minutes after midnight.
 */
public final class ZaehldauerIntervalle {

    private ZaehldauerIntervalle() {
    }

    /**
     * A single interval slot.
     *
     * @param nummer 1-based interval number within the day (1..96)
     * @param startUhrzeit start time "HH:mm"
     * @param endeUhrzeit end time "HH:mm" (the last slot of the day ends at "24:00")
     */
    public record Slot(int nummer, String startUhrzeit, String endeUhrzeit) {
    }

    /**
     * @return the ordered interval slots that are counted for the given Zähldauer.
     */
    public static List<Slot> slotsFor(final Zaehldauer zaehldauer) {
        return switch (zaehldauer) {
        case DAUER_24_STUNDEN -> range(1, 96);
        case DAUER_16_STUNDEN -> range(25, 88); // 06:00 - 22:00
        case DAUER_13_STUNDEN -> range(25, 76); // 06:00 - 19:00
        case DAUER_2_X_4_STUNDEN -> concat(range(25, 40), range(61, 76)); // 06-10 + 15-19
        case SONSTIGE -> range(1, 96); // plausibility check is disabled for SONSTIGE
        };
    }

    private static List<Slot> concat(final List<Slot> a, final List<Slot> b) {
        final List<Slot> all = new ArrayList<>(a);
        all.addAll(b);
        return all;
    }

    private static List<Slot> range(final int firstInclusive, final int lastInclusive) {
        final List<Slot> slots = new ArrayList<>();
        for (int n = firstInclusive; n <= lastInclusive; n++) {
            slots.add(new Slot(n, time((n - 1) * 15), time(n * 15)));
        }
        return slots;
    }

    private static String time(final int minutesFromMidnight) {
        final int hours = minutesFromMidnight / 60;
        final int minutes = minutesFromMidnight % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
