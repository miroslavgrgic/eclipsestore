package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.booking.outgoing.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BookingService {

    private final DatabaseAdapter databaseAdapter;
    private final GuestAdapter guestAdapter;
    private final RoomAdapter roomAdapter;
    private final PricingAdapter pricingAdapter;
    private final PaymentAdapter paymentAdapter;

    private final BookingEventPublisher bookingEventPublisher;

    public BookingService(DatabaseAdapter databaseAdapter, GuestAdapter guestAdapter, RoomAdapter roomAdapter, PricingAdapter pricingAdapter, PaymentAdapter paymentAdapter, BookingEventPublisher bookingEventPublisher) {
        this.databaseAdapter = databaseAdapter;
        this.guestAdapter = guestAdapter;
        this.roomAdapter = roomAdapter;
        this.pricingAdapter = pricingAdapter;
        this.paymentAdapter = paymentAdapter;
        this.bookingEventPublisher = bookingEventPublisher;
    }

    public List<Booking> getAllBookings() {
        return databaseAdapter.getBookings();
    }

    public Optional<Booking> getBooking(UUID id) {
        return databaseAdapter.getBooking(id);
    }

    public Booking createBooking(Booking booking) {
        // validate booking
        if (! booking.isValid(booking)) {
            throw new IllegalArgumentException("Booking is invalid");
        }

        // update guests
        guestAdapter.upsert(booking.getGuests());

        // check if room exists
        var room = roomAdapter.findById(booking.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + booking.getRoom().getId() + " does not exist"));
        // and its capacity
        if (room.maxNumberOfGuests() < booking.getGuests().size()) {
            throw new IllegalArgumentException("Too many guests for this room");
        }

        // fetch actual room price depending on dates
        booking.setPrice(pricingAdapter.getPriceOfRoom(booking.getRoom().getId(), booking.getFrom(), booking.getTo()));

        // adding to bookings
        databaseAdapter.save(booking);

        // create Domain event
        bookingEventPublisher.publishBookingEvent(booking);

        log.info("Created booking: {}", booking);
        return booking;
    }

    public void deleteBookingByID(UUID bookingID) {
        databaseAdapter.deleteBookingByID(bookingID);
        log.info("Deleted Booking with ID {}", bookingID);
    }

}
