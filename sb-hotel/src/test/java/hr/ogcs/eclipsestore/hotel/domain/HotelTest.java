package hr.ogcs.eclipsestore.hotel.domain;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.guest.Address;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelTest {

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = new Hotel("Mianmare", true);
    }

    @Test
    void should_return_that_hotel_is_accepting_credit_cards() {
        assertTrue(hotel.acceptsCreditCards());
    }

    @Test
    void should_return_that_booking_is_valid() {
        Booking booking = Booking.builder()
                .from(LocalDate.now().plusDays(2))
                .room(createRoom("Gloria"))
                .guests(createGuests())
                .build();

        assertTrue(hotel.isBookingValid(booking));
    }

    @Test
    void should_return_that_hotel_is_not_handicap_friendly() {
        hotel.getRooms().add(createRoom("Gloria"));
        hotel.getRooms().add(createRoom("Fauna"));
        hotel.getRooms().add(createRoom("Victoria"));

        assertTrue(hotel.isHandicapFriendlyHotel());
    }

    private Room createRoom(String name) {
        return Room.builder()
                .name(name)
                .price(BigDecimal.valueOf(100.00))
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