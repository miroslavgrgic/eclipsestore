package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BookingService {

    private final StorageService storageService;

    private final GuestAdapter guestAdapter;
    private final RoomAdapter roomAdapter;
    private final PricingAdapter pricingAdapter;
    private final PaymentAdapter paymentAdapter;

    private final BookingEventPublisher bookingEventPublisher;

    public BookingService(StorageService storageService, GuestAdapter guestAdapter, RoomAdapter roomAdapter, PricingAdapter pricingAdapter, PaymentAdapter paymentAdapter, BookingEventPublisher bookingEventPublisher) {
        this.storageService = storageService;
        this.guestAdapter = guestAdapter;
        this.roomAdapter = roomAdapter;
        this.pricingAdapter = pricingAdapter;
        this.paymentAdapter = paymentAdapter;
        this.bookingEventPublisher = bookingEventPublisher;
    }

    public List<Booking> getAllBookings() {
        return storageService.hotel.getBookings();
    }

    public Optional<Booking> getBooking(UUID id) {
        return storageService.hotel.getBookings().stream().filter(booking -> booking.getId().equals(id)).findFirst();
    }

    public Booking createBooking(Booking booking) {
        // validate booking
        if (! BookingRules.isBookingValid(booking)) {
            throw new IllegalArgumentException("Booking is invalid");
        }

        // check if guest exists
        var guests = guestAdapter.find(booking.getGuests());

        // check if room exists
        var room = roomAdapter.findById(booking.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + booking.getRoom().getId() + " does not exist"));
        // and its capacity
        if (room.maxNumberOfGuests() < booking.getGuests().size()) {
            throw new IllegalArgumentException("Too many guests for this room");
        }

        // fetch actual room price
        var actualPrice = pricingAdapter.getPriceOfRoom(booking.getRoom().getId(), booking.getFrom(), booking.getTo());
        booking.setPrice(actualPrice);

        // entity needs a primary key
        booking.setId(UUID.randomUUID());

        // adding to bookings
        storageService.hotel.getBookings().add(booking);

        // STORE IT!
        storageService.store(storageService.hotel.getBookings());
        // create Domain event
        bookingEventPublisher.publishBookingEvent(booking);

        log.info("Created booking: {}", booking);
        return booking;
    }

    public void deleteBookingByID(UUID bookingID) {
        Optional<Booking> booking = storageService.hotel.getBookings().stream()
                .filter(item -> item.getId().equals(bookingID))
                .findFirst();

        if (booking.isEmpty()) {
            throw new IllegalArgumentException("Trying to delete Booking " + bookingID + " that does not exist!");
        } else {
            storageService.hotel.getBookings().remove(booking.get());
            storageService.storageManager.store(storageService.hotel.getBookings());
            log.info("Deleted Booking with ID {}", bookingID);
        }
    }

}
