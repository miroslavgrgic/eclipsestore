package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.model.Guest;
import hr.ogcs.eclipsestore.hotel.service.GuestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping(value = "/guests")
@Slf4j
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    public ResponseEntity<Guest> createGuest(@RequestBody Guest guest) {
        Guest newGuest = guestService.createGuest(guest);
        return ResponseEntity.status(HttpStatus.OK).body(newGuest);
    }

    @GetMapping
    public List<Guest> getAllGuests() {
        return guestService.getAllGuests();
    }

    @GetMapping(params = "limit")
    public List<Guest> getSubsetOfGuests(Integer limit) {
        return guestService.getAllGuests().stream().limit((limit == null ? 0 : limit)).toList();
    }

    @GetMapping(path = "/{id}")
    public Guest getGuestById(@PathVariable("id") UUID id) {
        return guestService.getAllGuests().stream()
                .filter(guest -> guest.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Guest with given ID not found"));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteGuest(@PathVariable(value = "id") String id) {
        guestService.deleteGuestByID(id);
        return ResponseEntity.accepted().build();
    }

    // It is not a valid REST endpoint -  just using for creating load on EclipseStore
    @PostMapping(path = "/bamm")
    public ResponseEntity<Guest> createGuests() {
        for (int bulk = 0; bulk < 10; bulk++) {
            List<Guest> guests = new ArrayList<>();
            for (int i = 0; i < 100_000; i++) {
                var guest = Guest.builder()
                        .firstName(UUID.randomUUID().toString())
                        .lastName(UUID.randomUUID().toString())
                        .age(new Random().nextInt(100))
                        .build();
                guests.add(guest);
            }
            guestService.createGuests(guests);
            log.info("Created 100.000 guests");
        }

        return ResponseEntity.created(URI.create("/guests")).build();
    }

    // Not REST, but helpful
    @GetMapping("/count")
    public ResponseEntity<Integer> getAllGuestsCount() {
        return ResponseEntity.ok(getAllGuests().size());
    }

}
