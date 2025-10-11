package hr.ogcs.eclipsestore.hotel.domain.booking.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public final class BookingPortImpl implements BookingPort {

    @Autowired
    BookingService bookingService;

    @Override
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @Override
    public Optional<Booking> getBooking(UUID id) {
        return bookingService.getAllBookings().stream()
                .filter(booking -> booking.getId().equals(id))
                .findFirst();
    }

    @Override
    public Booking createBooking(Booking booking) {
        return bookingService.createBooking(booking);
    }

    @Override
    public void deleteBookingByID(UUID id) {
        bookingService.deleteBookingByID(id);
    }

}
