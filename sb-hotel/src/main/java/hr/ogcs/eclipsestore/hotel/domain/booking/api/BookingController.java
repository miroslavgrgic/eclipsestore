package hr.ogcs.eclipsestore.hotel.domain.booking.api;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.incoming.BookingPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/bookings")
public class BookingController {

    private final BookingPort bookingPort;

    public BookingController(BookingPort bookingPort) {
        this.bookingPort = bookingPort;
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingPort.getAllBookings();
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking newBooking = bookingPort.createBooking(booking);
        return ResponseEntity.status(HttpStatus.OK).body(newBooking);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteBooking(@PathVariable(value = "id") UUID id) {
        bookingPort.deleteBookingByID(id);
        return ResponseEntity.accepted().build();
    }
}
