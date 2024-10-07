package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Address;
import hr.ogcs.eclipsestore.hotel.model.Booking;
import hr.ogcs.eclipsestore.hotel.model.Guest;
import hr.ogcs.eclipsestore.hotel.model.Room;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class BookingIntegrationTest {

    private static BookingService bookingService;
    private static RoomService roomService;
    private static GuestService guestService;

    @BeforeAll
    static void setup() {
        var storageService = new StorageService("test");
        roomService = new RoomService(storageService);
        guestService = new GuestService(storageService);
        bookingService = new BookingService(storageService, guestService, roomService);
    }

    @Test
    void should_fail_booking_when_room_does_not_exist() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(Booking.builder()
                            .date(LocalDate.now())
                            .room(Room.builder().id(UUID.randomUUID()).build())
                            .guests(List.of(Guest.builder().firstName("Max").lastName("Mustermann").build()))
                            .build());
        });
        Assertions.assertTrue(thrown.getMessage().contains("Room with ID "));
        Assertions.assertTrue(thrown.getMessage().contains(" does not exist"));
    }

    @Test
    void should_create_Booking() {
        // given
        var roomId = UUID.randomUUID();
        var room = roomService.createRoom(Room.builder()
                .id(roomId)
                .name("Nice Room")
                .sqm(120)
                .bedSizes(List.of("90 x 200"))
                .state(Room.State.FREE)
                .availableSince(LocalDate.now().minusYears(1))
                .price(BigDecimal.valueOf(100))
                .build());

        var guest = Guest.builder()
                .firstName("Max")
                .lastName("Mustermann")
                .age(34)
                .address(Address.builder()
                        .street("Ulica Petra Preradovića 225")
                        .postalCode(31400)
                        .city("Đakovo")
                        .state("HR")
                        .build())
                .build();
        List<Guest> guests = new ArrayList<>();
        guests.add(guest);

        // when
        var result = bookingService.createBooking(Booking.builder()
                .room(room)
                .guests(guests)
                .date(LocalDate.now())
                .build());

        // then
        Assertions.assertInstanceOf(UUID.class, result.getId());
        Assertions.assertEquals(result.getRoom(), roomService.findById(roomId).get());
    }

}