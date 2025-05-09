package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingRulesTest {

    @Test
    void isBookingValid() {
        var booking = Booking.builder()
                .from(LocalDate.now().plusDays(2))
                .to(LocalDate.now())
                .guests(List.of(Guest.builder().lastName("Mustermann").build()))
                .room(Room.builder().name("Golden Suite").build())
                .build();

        Assertions.assertFalse(BookingRules.isBookingValid(booking));
    }
}