package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.guest.Address;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingTest {

    private Booking booking;

    @Test
    void should_create_booking_for_2_guests_in_room_gloria() {
        booking = Booking.builder()
                .from(LocalDate.now().plusDays(2))
                .to(LocalDate.now().plusDays(7))
                .room(createGloria())
                .guests(createGuests())
                .build();

        assertEquals(2, booking.getGuests().size());
        assertEquals(2, booking.getRoom().maxNumberOfGuests());
        assertTrue(booking.getRoom().canBeUsedWithHandicaps());
        assertEquals(BigDecimal.valueOf(100.00), booking.getRoom().getDefaultPrice());
    }

    private Room createGloria() {
        return Room.builder()
                .name("Gloria")
                .defaultPrice(BigDecimal.valueOf(100.00))
                .sqm(55)
                .canBeUsedWithHandicaps(true)
                .build();
    }

    private List<Guest> createGuests() {
        var guest1 = Guest.builder()
                .firstName("Sam")
                .lastName("Oldman")
                .age(34)
                .address(Address.builder()
                        .street("Ulica Petra Preradovića 225")
                        .postalCode(31400)
                        .city("Đakovo")
                        .state("HR")
                        .build())
                .build();
        var guest2 = Guest.builder()
                .firstName("Ana")
                .lastName("Oldman")
                .age(33)
                .build();
        return List.of(guest1, guest2);
    }

}