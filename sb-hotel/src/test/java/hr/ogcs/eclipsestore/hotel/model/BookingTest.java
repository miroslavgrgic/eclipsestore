package hr.ogcs.eclipsestore.hotel.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
                .date(LocalDate.now().plusDays(2))
                .room(createGloria())
                .guests(createGuests())
                .build();

        assertEquals(2, booking.getGuests().size());
        assertEquals(2, booking.getRoom().maxNumberOfGuests());
        assertTrue(booking.getRoom().isCanBeUsedWithHandicaps());
        assertEquals(BigDecimal.valueOf(100.00), booking.getRoom().getPrice());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        try {
            System.out.println(objectMapper.writeValueAsString(booking));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Room createGloria() {
        return Room.builder()
                .name("Gloria")
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