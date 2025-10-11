package hr.ogcs.eclipsestore.hotel.domain.guest.incoming;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuestPort {

    List<Guest> getAllGuests();
    Optional<Guest> findById(UUID id);
    Optional<Guest> findByLastname(String lastName);

    // FIXME too much power for the users of this interface!!
    Guest createGuest(Guest guest);
    void deleteGuestByID(UUID id);

}
