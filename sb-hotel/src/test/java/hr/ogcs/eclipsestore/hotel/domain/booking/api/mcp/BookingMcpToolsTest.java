package hr.ogcs.eclipsestore.hotel.domain.booking.api.mcp;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.BookingService;
import hr.ogcs.eclipsestore.hotel.domain.booking.incoming.BookingPortImpl;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingMcpToolsTest {

    private BookingMcpTools bookingMcpTools;

    private Booking aprilBooking;
    private Booking marchBooking;
    private Booking spanningBooking;

    @BeforeEach
    void setUp() {
        BookingService bookingServiceMock = Mockito.mock(BookingService.class);
        BookingPortImpl bookingPort = new BookingPortImpl();
        ReflectionTestUtils.setField(bookingPort, "bookingService", bookingServiceMock);
        bookingMcpTools = new BookingMcpTools(bookingPort);

        Room room = Room.builder().name("Sea View").build();
        Guest guestOne = Guest.builder().firstName("Max").lastName("Mustermann").age(30).build();
        Guest guestTwo = Guest.builder().firstName("Marina").lastName("Musterfrau").age(28).build();

        aprilBooking = Booking.builder()
                .room(room)
                .guests(List.of(guestOne, guestTwo))
                .from(LocalDate.of(2026, 4, 10))
                .to(LocalDate.of(2026, 4, 15))
                .price(BigDecimal.valueOf(500))
                .paymentStatus(Booking.PaymentStatus.PAID)
                .build();

        marchBooking = Booking.builder()
                .room(room)
                .guests(List.of(guestOne))
                .from(LocalDate.of(2026, 3, 1))
                .to(LocalDate.of(2026, 3, 5))
                .price(BigDecimal.valueOf(200))
                .build();

        spanningBooking = Booking.builder()
                .room(room)
                .guests(List.of(guestOne))
                .from(LocalDate.of(2026, 3, 28))
                .to(LocalDate.of(2026, 4, 2))
                .price(BigDecimal.valueOf(300))
                .build();

        Mockito.when(bookingServiceMock.getAllBookings())
                .thenReturn(List.of(aprilBooking, marchBooking, spanningBooking));
    }

    @Test
    void should_return_all_bookings_when_no_date_range_given() {
        List<BookingSummary> result = bookingMcpTools.getBookings(null, null);

        assertEquals(3, result.size());
    }

    @Test
    void should_return_only_bookings_overlapping_given_range() {
        List<BookingSummary> result = bookingMcpTools.getBookings(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertEquals(2, result.size());
        assertEquals(
                List.of(aprilBooking.getId(), spanningBooking.getId()),
                result.stream().map(BookingSummary::id).toList()
        );
    }

    @Test
    void should_map_guest_names_and_count() {
        List<BookingSummary> result = bookingMcpTools.getBookings(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        BookingSummary summary = result.stream()
                .filter(b -> b.id().equals(aprilBooking.getId()))
                .findFirst().orElseThrow();

        assertEquals(2, summary.guestCount());
        assertEquals(List.of("Max Mustermann", "Marina Musterfrau"), summary.guestNames());
        assertEquals("Sea View", summary.roomName());
    }
}
