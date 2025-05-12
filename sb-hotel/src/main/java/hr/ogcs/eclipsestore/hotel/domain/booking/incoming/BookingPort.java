package hr.ogcs.eclipsestore.hotel.domain.booking.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;

import java.util.Optional;
import java.util.UUID;

public sealed interface BookingPort permits BookingPortImpl {

    Optional<Booking> getBooking(final UUID id);
    UUID createBooking(final Booking booking);

}
