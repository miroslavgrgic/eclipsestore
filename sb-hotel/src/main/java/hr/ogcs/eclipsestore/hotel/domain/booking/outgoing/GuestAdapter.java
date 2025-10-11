package hr.ogcs.eclipsestore.hotel.domain.booking.outgoing;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.incoming.GuestPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GuestAdapter {

    final GuestPort guestPort;

    public GuestAdapter(GuestPort guestPort) {
        this.guestPort = guestPort;
    }

    public UUID createGuest(Guest guest) {
        return guestPort.createGuest(guest).getId();
    }

    public void upsert(List<Guest> guests) {
        guests.stream()
                .filter(guest -> guestPort.findById(guest.getId()).isEmpty())
                .forEach(guest -> guest.setId(guestPort.createGuest(guest).getId()));
    }

    public Optional<Guest> findByLastname(String lastname) {
        return guestPort.findByLastname(lastname);
    }

}
