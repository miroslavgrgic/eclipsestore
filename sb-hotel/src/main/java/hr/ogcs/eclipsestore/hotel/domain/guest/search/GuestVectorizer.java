package hr.ogcs.eclipsestore.hotel.domain.guest.search;

import hr.ogcs.eclipsestore.hotel.domain.guest.Address;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import org.eclipse.store.gigamap.jvector.Vectorizer;

/**
 * Turns a {@link Guest} into a deterministic feature vector: no ML model or external service
 * involved, so the same guest data always produces the same vector and search results are
 * reproducible. Guests end up close together if they are similar in age, booker role, home
 * city/state and last name (e.g. likely family members or repeat travel companions).
 */
public class GuestVectorizer extends Vectorizer<Guest> {

    public static final int DIMENSION = 64;

    private static final int MAX_AGE = 120;

    private static final int LOCATION_OFFSET = 3;
    private static final int LOCATION_SLOTS = 32;
    private static final int NAME_OFFSET = LOCATION_OFFSET + LOCATION_SLOTS;
    private static final int NAME_SLOTS = DIMENSION - NAME_OFFSET;

    @Override
    public float[] vectorize(Guest guest) {
        float[] vector = new float[DIMENSION];

        vector[0] = Math.clamp(guest.getAge(), 0, MAX_AGE) / (float) MAX_AGE;
        vector[1] = guest.isTheBooker() ? 1f : 0f;
        vector[2] = normalizedPostalCode(guest.getAddress());

        hashInto(vector, LOCATION_OFFSET, LOCATION_SLOTS, locationText(guest.getAddress()));
        hashInto(vector, NAME_OFFSET, NAME_SLOTS, guest.getLastName());

        return vector;
    }

    @Override
    public boolean isEmbedded() {
        return false;
    }

    private static float normalizedPostalCode(Address address) {
        if (address == null || address.getPostalCode() == null) {
            return 0f;
        }
        return (address.getPostalCode() % 100_000) / 100_000f;
    }

    private static String locationText(Address address) {
        if (address == null) {
            return "";
        }
        return String.join(" ",
                address.getCity() == null ? "" : address.getCity(),
                address.getState() == null ? "" : address.getState());
    }

    // Hashes lowercased character trigrams of the text into `slots` buckets starting at `offset`
    // (a fixed-size bag-of-trigrams), then L2-normalizes that block so it contributes a bounded,
    // comparable amount of cosine-similarity "weight" regardless of text length.
    private static void hashInto(float[] vector, int offset, int slots, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String normalized = " " + text.toLowerCase().trim().replaceAll("\\s+", " ") + " ";
        for (int i = 0; i < normalized.length() - 2; i++) {
            String trigram = normalized.substring(i, i + 3);
            int slot = offset + Math.floorMod(trigram.hashCode(), slots);
            vector[slot] += 1f;
        }
        normalize(vector, offset, slots);
    }

    private static void normalize(float[] vector, int offset, int length) {
        double sumSquares = 0;
        for (int i = offset; i < offset + length; i++) {
            sumSquares += (double) vector[i] * vector[i];
        }
        if (sumSquares == 0) {
            return;
        }
        float norm = (float) Math.sqrt(sumSquares);
        for (int i = offset; i < offset + length; i++) {
            vector[i] /= norm;
        }
    }
}
