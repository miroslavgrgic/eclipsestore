package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.RoomService;
import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BookingService {

    private final StorageService storageService;
    private final GuestAdapter guestAdapter;
    private final RoomAdapter roomAdapter;

    public BookingService(StorageService storageService, GuestAdapter guestAdapter, RoomAdapter roomAdapter) {
        this.storageService = storageService;
        this.guestAdapter = guestAdapter;
        this.roomAdapter = roomAdapter;
    }

    public List<Booking> getAllBookings() {
        return storageService.hotel.getBookings();
    }

    public Booking createBooking(Booking booking) {

        if (! BookingRules.isBookingValid(booking)) {
            throw new IllegalArgumentException("Booking is invalid");
        }

        // check if room exists
        var room = roomAdapter.findById(booking.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + booking.getRoom().getId() + " does not exist"));

        if (room.maxNumberOfGuests() < booking.getGuests().size()) {
            throw new IllegalArgumentException("Too many guests for this room");
        }

        // check if guest already exists
        List<Guest> potentialNewGuests = new ArrayList<>();
        booking.getGuests().stream().forEach(
                guest -> {
                    // TODO last name is not enough - an dedicated equals could handle it
                    guestAdapter.findByLastname(guest.getLastName())
                        .ifPresent(g -> potentialNewGuests.add(g));
                }
        );

        if (!potentialNewGuests.isEmpty()) {
            booking.getGuests().removeAll(booking.getGuests());
            booking.getGuests().addAll(potentialNewGuests);
        } else {
            booking.getGuests().stream().forEach(guest -> {
                guest.setId(UUID.randomUUID());
                // STORING the new guest in its domain
                guestAdapter.createGuest(guest);
            });
        }

        booking.setId(UUID.randomUUID());
        storageService.hotel.getBookings().add(booking);

        // STORE IT!
        storageService.store(storageService.hotel.getBookings());
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
