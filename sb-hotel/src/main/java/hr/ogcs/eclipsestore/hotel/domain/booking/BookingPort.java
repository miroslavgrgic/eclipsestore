package hr.ogcs.eclipsestore.hotel.domain.booking;

import java.util.Optional;
import java.util.UUID;

public interface BookingPort {

    Optional<Booking> getBooking(final UUID id);
    UUID createBooking(final Booking booking);

}
