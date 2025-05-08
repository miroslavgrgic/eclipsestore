package hr.ogcs.eclipsestore.hotel;

import hr.ogcs.eclipsestore.hotel.domain.booking.BookingService;
import hr.ogcs.eclipsestore.hotel.domain.booking.GuestAdapter;
import hr.ogcs.eclipsestore.hotel.domain.guest.Address;
import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.GuestService;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import hr.ogcs.eclipsestore.hotel.domain.room.RoomService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BookingIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired RoomService roomService;
    @Autowired GuestAdapter guestAdapter;
    @Autowired GuestService guestService;

    @BeforeAll
    static void setup() {
//        var storageService = new StorageService("integration-test");
//        roomService = new RoomService(storageService);
//        guestAdapter = new GuestAdapter();
//        guestService = new GuestService(storageService);
//        bookingService = new BookingService(storageService, guestAdapter, roomService);
    }

    @Test
    void should_fail_booking_when_room_does_not_exist() {
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(Booking.builder()
                            .from(LocalDate.now())
                            .to(LocalDate.now().plusDays(5))
                            // this UUID provokes the exception
                            .room(Room.builder().id(UUID.randomUUID()).build())
                            .guests(List.of(Guest.builder().firstName("Max").lastName("Mustermann").build()))
                            .build());
        });
        assertTrue(thrown.getMessage().matches("Room with ID .* does not exist"));
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
                .from(LocalDate.now())
                .build());

        // then
        Assertions.assertInstanceOf(UUID.class, result.getId());
        Assertions.assertEquals(result.getRoom(), roomService.findById(roomId).get());
    }

}