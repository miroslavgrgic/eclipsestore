package hr.ogcs.eclipsestore.consumer.api;

import hr.ogcs.eclipsestore.consumer.model.Booking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ConsumerController {

    @PostMapping(consumes = "application/java", produces = "application/java")
    public ResponseEntity receiverBooking(@RequestBody Booking booking) {
        System.out.println("Got it!" + booking.toString());
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public List<Booking> getBookings() {
        return Collections.emptyList();
    }

}
