package hr.ogcs.eclipsestore.hotel;

import hr.ogcs.eclipsestore.hotel.model.Booking;
import hr.ogcs.eclipsestore.hotel.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking newBooking = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.OK).body(newBooking);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteBooking(@PathVariable(value = "id") String id) {
        bookingService.deleteBookingByID(id);
        return ResponseEntity.accepted().build();
    }
}
