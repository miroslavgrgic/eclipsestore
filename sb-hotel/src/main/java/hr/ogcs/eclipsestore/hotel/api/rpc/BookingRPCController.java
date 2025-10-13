package hr.ogcs.eclipsestore.hotel.api.rpc;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.incoming.BookingPort;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/rpc/bookings")
@OpenAPIDefinition()
public class BookingRPCController {

    private final BookingPort bookingPort;

    public BookingRPCController(BookingPort bookingPort) {
        this.bookingPort = bookingPort;
    }

    @Tag(name = "Create a Booking.",
         description = "The returned Booking entity will contain a unique identifier for further processing.")
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking newBooking = bookingPort.createBooking(booking);
        return ResponseEntity.status(HttpStatus.OK).body(newBooking);
    }

    @Tag(name = "Cancel and refund a Booking.",
         description = "The returned Booking entity will contain a unique identifier for further processing.")
    @PostMapping(value = "cancelAndRefund")
    public ResponseEntity<Booking> cancelAndRefund(@RequestParam UUID id, @RequestParam RefundOption refundOption) {
        // TODO not yet implemented
        return ResponseEntity.accepted().build();
    }

    @Tag(name = "Export Bookings by given date range.",
            description = "Date can be on daily, monthly or year base. The CSV will contain the fields:" +
                    "'id, roomNumber, guestName, from, to, price' separated by semicolons.")
    @PostMapping(value = "exportAllBookingsAsCSV", produces = "text/csv")
    public ResponseEntity<List<Object>> exportAllBookingsAsCSV(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        // TODO not yet implemented
        return ResponseEntity.accepted().build();
    }

    @Tag(name = "Export Bookings by given date range.",
            description = "Date can be on daily, monthly or year base. The file will contain the fields:" +
                    "'id, roomNumber, guestName, from, to, price'.")
    @PostMapping(value = "/exportAllBookingsByGivenFormat")
    public ResponseEntity<List<Object>> exportAllBookingsByGivenFormat(
            @RequestParam ExportFormat format,
            @RequestParam LocalDate from, @RequestParam LocalDate to) {
        // TODO not yet implemented
        return ResponseEntity.accepted().build();
    }


    @Tag(name = "Archive a Booking for long term persistence. ",
            description = "The archived Booking will not be available at API level any more.")
    @PostMapping(value = "archiveBooking")
    public ResponseEntity<Booking> archiveBooking(@RequestParam UUID id) {
        bookingPort.archiveBookingByID(id);
        return ResponseEntity.accepted().build();
    }

    @Tag(name = "Archive a Booking for long term persistence. ",
            description = "The archived Booking will not be available at API level any more.")
    @GetMapping(value = "archiveBooking/{id}")
    public ResponseEntity<Object> archiveBookingV2(@PathVariable(value = "id") UUID id) {
        bookingPort.archiveBookingByID(id);
        return ResponseEntity.accepted().build();
    }

    public enum ExportFormat {
        CVS,
        XLS,
        JSON
    }
    public enum RefundOption {
        PAYBACK,
        VOUCHER,
        NONE
    }

}
