package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.model.Guest;
import hr.ogcs.eclipsestore.hotel.service.GuestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/guests")
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

    @DeleteMapping(value = "/{id}")
    public ResponseEntity deleteGuest(@PathVariable(value = "id") String id) {
        guestService.deleteGuestByID(id);
        return ResponseEntity.accepted().build();
    }
}
