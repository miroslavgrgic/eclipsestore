package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.GuestPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GuestAdapter {

    final GuestPort guestPort;

    public GuestAdapter(GuestPort guestPort) {
        this.guestPort = guestPort;
    }

    public Optional<Guest> findByLastname(String lastname) {
        return guestPort.findByLastname(lastname);
    }

    public Guest createGuest(Guest guest) {
        return guestPort.createGuest(guest);
    }

}
