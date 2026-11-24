package hr.ogcs.eclipsestore.hotel.domain.filter;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityFilterTest {

    private final Guest sam = Guest.builder().firstName("Sam").lastName("Oldman").age(34).build();
    private final Guest ana = Guest.builder().firstName("Ana").lastName("Oldman").age(12).build();
    private final List<Guest> guests = List.of(sam, ana);

    private final Room gloria = Room.builder().name("Gloria").defaultPrice(BigDecimal.valueOf(100)).sqm(55)
            .canBeUsedWithHandicaps(true).state(Room.State.FREE).build();
    private final Room fauna = Room.builder().name("Fauna").defaultPrice(BigDecimal.valueOf(200)).sqm(30)
            .canBeUsedWithHandicaps(false).state(Room.State.BLOCKED).build();
    private final List<Room> rooms = List.of(gloria, fauna);

    @Test
    void returns_all_entities_when_criteria_is_empty_or_null() {
        assertEquals(guests, EntityFilter.filter(guests, null));
        assertEquals(guests, EntityFilter.filter(guests, Map.of()));
    }

    @Test
    void filters_by_equals() {
        List<Guest> result = EntityFilter.filter(guests, Map.of("lastName", "Oldman", "firstName", "Sam"));

        assertEquals(List.of(sam), result);
    }

    @Test
    void filters_by_contains_case_insensitively() {
        List<Guest> result = EntityFilter.filter(guests, Map.of("firstName_contains", "a"));

        assertEquals(List.of(sam, ana), result);
    }

    @Test
    void filters_by_range() {
        assertEquals(List.of(sam), EntityFilter.filter(guests, Map.of("age_gte", 18)));
        assertEquals(List.of(ana), EntityFilter.filter(guests, Map.of("age_lte", 12)));
    }

    @Test
    void filters_by_in() {
        Booking open = Booking.builder().paymentStatus(Booking.PaymentStatus.OPEN).build();
        Booking cancelled = Booking.builder().paymentStatus(Booking.PaymentStatus.CANCELLED).build();

        List<Booking> result = EntityFilter.filter(List.of(open, cancelled),
                Map.of("paymentStatus_in", List.of("OPEN", "PAID")));

        assertEquals(List.of(open), result);
    }

    @Test
    void filters_on_bigdecimal_and_boolean_fields() {
        assertEquals(List.of(fauna), EntityFilter.filter(rooms, Map.of("defaultPrice_gte", 150)));
        assertEquals(List.of(gloria), EntityFilter.filter(rooms, Map.of("canBeUsedWithHandicaps", true)));
    }

    @Test
    void rejects_unfilterable_field() {
        assertThrows(IllegalArgumentException.class,
                () -> EntityFilter.filter(guests, Map.of("address", "somewhere")));
    }

    @Test
    void rejects_unsupported_operator_for_field() {
        assertThrows(IllegalArgumentException.class,
                () -> EntityFilter.filter(guests, Map.of("firstName_gte", "A")));
    }

    @Test
    void filters_map_values_and_preserves_keys() {
        Map<String, Room> byKey = Map.of("free", gloria, "blocked", fauna);

        Map<String, Room> result = EntityFilter.filterValues(byKey, Map.of("state", "FREE"));

        assertTrue(result.containsKey("free"));
        assertEquals(1, result.size());
    }
}
