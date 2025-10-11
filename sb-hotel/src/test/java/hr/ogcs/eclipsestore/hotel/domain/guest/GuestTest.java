package hr.ogcs.eclipsestore.hotel.domain.guest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuestTest {

    private Guest guest;

    @Test
    void should_and_should_not_be_allowed_to_consume_alcohol() {
        guest = createGuest(34);
        assertTrue(guest.isAllowedToConsumeAlcohol());

        guest = createGuest(14);
        assertFalse(guest.isAllowedToConsumeAlcohol());
    }

    @Test
    void should_be_a_child() {
        guest = createGuest(12);
        assertTrue(guest.isChildren());
    }

    @Test
    void should_have_valid_data() {
        guest = createGuest(20);
        assertTrue(guest.hasValidData());
    }

    private Guest createGuest(int age) {
        return Guest.builder()
                .firstName("Max")
                .lastName("Mustermann")
                .age(age)
                .address(Address.builder()
                        .street("Ulica Petra Preradovića 225")
                        .postalCode(31400)
                        .city("Đakovo")
                        .state("HR")
                        .build())
                .build();
    }
}