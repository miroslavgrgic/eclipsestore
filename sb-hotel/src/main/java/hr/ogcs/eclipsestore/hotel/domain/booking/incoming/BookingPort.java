package hr.ogcs.eclipsestore.hotel.domain.booking.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public sealed interface BookingPort permits BookingPortImpl {

    List<Booking> getAllBookings();
    Optional<Booking> getBooking(final UUID id);
    Booking createBooking(final Booking booking);
    void deleteBookingByID(UUID id);

    void archiveBookingByID(UUID id);
}
