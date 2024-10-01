package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Booking;
import hr.ogcs.eclipsestore.hotel.model.Guest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BookingService {

    final StorageService storageService;
    final GuestService guestService;

    public BookingService(StorageService storageService, GuestService guestService) {
        this.storageService = storageService;
        this.guestService = guestService;
    }

    public List<Booking> getAllBookings() {
        return storageService.schema.getBookings();
    }

    public Booking createBooking(Booking booking) {

        List<Guest> potentialNewGuests = new ArrayList<>();
        booking.getGuests().stream().forEach(
                guest -> {
                    guestService.findByLastname(guest.getLastName())
                        .ifPresent(g -> potentialNewGuests.add(g));
                }
        );

        if (!potentialNewGuests.isEmpty()) {
            booking.getGuests().removeAll(booking.getGuests());
            booking.getGuests().addAll(potentialNewGuests);
        } else {
            booking.getGuests().stream().forEach(guest -> {
                guest.setId(UUID.randomUUID());
                guestService.createGuest(guest);
            });

        }

        storageService.schema.getBookings().add(booking);

        // STORE IT!
        booking.setId(UUID.randomUUID());
        storageService.store(booking);
        log.info("Created booking: {}", booking);

        return booking;
    }

    public void deleteBookingByID(String bookingID) {
        Optional<Booking> booking = storageService.schema.getBookings().stream()
                .filter(item -> item.getId().toString().equals(bookingID))
                .findFirst();

        if (booking.isEmpty()) {
            throw new IllegalArgumentException("Trying to delete entry that does not exist!");
        } else {
            storageService.schema.getBookings().remove(booking.get());
            storageService.storageManager.store(storageService.schema.getBookings());
            log.info("Deleted Booking with ID {}", bookingID);
        }
    }

}
