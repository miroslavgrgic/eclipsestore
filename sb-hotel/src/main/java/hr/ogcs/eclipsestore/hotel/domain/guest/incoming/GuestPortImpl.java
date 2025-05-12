package hr.ogcs.eclipsestore.hotel.domain.guest.incoming;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.GuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GuestPortImpl implements GuestPort {

    @Autowired
    GuestService guestService;

    @Override
    public Guest createGuest(Guest guest) {
        return guestService.createGuest(guest);
    }

    @Override
    public Optional<Guest> findById(UUID id) {
        return guestService.findById(id);
    }

    @Override
    public Optional<Guest> findByLastname(String lastName) {
        return guestService.findByLastname(lastName);
    }

}
