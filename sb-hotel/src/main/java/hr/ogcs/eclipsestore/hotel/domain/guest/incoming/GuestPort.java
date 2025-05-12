package hr.ogcs.eclipsestore.hotel.domain.guest.incoming;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;

import java.util.Optional;
import java.util.UUID;

public interface GuestPort {

    Guest createGuest(Guest guest);
    Optional<Guest> findById(UUID id);
    Optional<Guest> findByLastname(String lastName);

}
