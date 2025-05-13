package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.GuestService;
import hr.ogcs.eclipsestore.hotel.domain.guest.incoming.GuestPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@RestController
@RequestMapping(value = "/guests")
@Slf4j
public class GuestController {

    private final GuestPort guestPort;


    public GuestController(GuestPort guestPort, GuestService guestService) {
        this.guestPort = guestPort;
        this.guestService = guestService;
    }

    @PostMapping
    public ResponseEntity<UUID> createGuest(@RequestBody Guest guest) {
        Guest newGuest = guestPort.createGuest(guest);
        return ResponseEntity.status(HttpStatus.OK).body(newGuest.getId());
    }

    @GetMapping
    public List<Guest> getAllGuests() {
        return guestPort.getAllGuests();
    }

    @GetMapping(params = "limit")
    public List<Guest> getSubsetOfGuests(Integer limit) {
        return guestPort.getAllGuests().stream().limit((limit == null ? 0 : limit)).toList();
    }

    @GetMapping(path = "/{id}")
    public Guest getGuestById(@PathVariable("id") UUID id) {
        return guestPort.getAllGuests().stream()
                .filter(guest -> guest.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Guest with given ID not found"));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteGuest(@PathVariable(value = "id") UUID id) {
        guestPort.deleteGuestByID(id);
        return ResponseEntity.accepted().build();
    }

    // Bypassing the DDD world now...
    // just using for direct creation of bunch of data
    private final GuestService guestService;

    // It is not a valid REST endpoint -  just using for creating load on EclipseStore
    @PostMapping(path = "/bamm")
    public ResponseEntity<Guest> createGuests() {
        IntStream.range(0, 10).boxed().forEach(bulk -> {
            List<Guest> guests = new ArrayList<>();
            IntStream.range(0, 100_000).boxed().forEach(iter -> {
                var guest = Guest.builder()
                        .firstName(UUID.randomUUID().toString())
                        .lastName(UUID.randomUUID().toString())
                        .age(new Random().nextInt(100))
                        .build();
                guests.add(guest);
            });
            guestService.createGuests(guests);
            log.info("Created 100.000 guests");
        });
        return ResponseEntity.created(URI.create("/guests")).build();
    }

    // Not REST, but helpful
    @GetMapping("/count")
    public ResponseEntity<Integer> getAllGuestsCount() {
        return ResponseEntity.ok(getAllGuests().size());
    }

}
