package hr.ogcs.eclipsestore.hotel.domain.guest;

import java.util.Optional;
import java.util.UUID;

public interface GuestPort {

    UUID createGuest(Guest guest);
    Optional<Guest> findById(UUID id);
    Optional<Guest> findByLastname(String lastName);

}
