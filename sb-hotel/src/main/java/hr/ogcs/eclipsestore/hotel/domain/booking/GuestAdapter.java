package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.GuestPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public List<Guest> find(List<Guest> guests) {
        List<Guest> existingGuests = new ArrayList<>();
        guests.forEach(guest -> guestPort.findById(guest.getId())
                .ifPresentOrElse(existingGuests::add, () -> existingGuests.add(guestPort.createGuest(guest))));
        return existingGuests;
    }

    public Optional<Guest> findByLastname(String lastname) {
        return guestPort.findByLastname(lastname);
    }

}
