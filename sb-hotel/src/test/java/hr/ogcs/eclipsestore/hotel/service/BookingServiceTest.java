package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.BookingDto;
import hr.ogcs.eclipsestore.hotel.model.Guest;
import hr.ogcs.eclipsestore.hotel.model.Room;
import hr.ogcs.eclipsestore.hotel.repository.BookingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    @Mock
    private RoomService roomService;

    @Test
    void should_fail_booking_when_room_does_not_exist() {
        Mockito.when(roomService.findById(Mockito.any())).thenReturn(Optional.empty());

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Room room = new Room();
            room.setId(UUID.randomUUID());

            bookingService.createBooking(BookingDto.builder()
                            .date(LocalDate.now())
                            .room(room)
                            .guests(List.of(new Guest()))
                            .build());
        });
        Assertions.assertTrue(thrown.getMessage().contains("Room with ID "));
        Assertions.assertTrue(thrown.getMessage().contains(" does not exist"));
    }

}