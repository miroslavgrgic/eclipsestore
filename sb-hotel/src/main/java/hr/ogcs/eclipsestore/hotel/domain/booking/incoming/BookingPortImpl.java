package hr.ogcs.eclipsestore.hotel.domain.booking.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public final class BookingPortImpl implements BookingPort {

    @Autowired
    BookingService bookingService;

    @Override
    public Optional<Booking> getBooking(UUID id) {
        return bookingService.getAllBookings().stream()
                .filter(booking -> booking.getId().equals(id))
                .findFirst();
    }

    @Override
    public UUID createBooking(Booking booking) {
        return bookingService.createBooking(booking).getId();
    }

}
