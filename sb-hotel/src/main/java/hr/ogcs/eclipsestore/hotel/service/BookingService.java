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

    private StorageService storageService;
    private GuestService guestService;
    private RoomService roomService;

    public BookingService(StorageService storageService, GuestService guestService, RoomService roomService) {
        this.storageService = storageService;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    public List<Booking> getAllBookings() {
        return storageService.schema.getBookings();
    }

    public Booking createBooking(Booking booking) {
        // check if room exists
        roomService.findById(booking.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + booking.getRoom().getId() + " does not exist"));

        if (booking.getRoom().maxNumberOfGuests() < booking.getGuests().size()) {
            throw new IllegalArgumentException("Too many guests for this room");
        }

        // check if guest already exists
        List<Guest> potentialNewGuests = new ArrayList<>();
        booking.getGuests().stream().forEach(
                guest -> {
                    // TODO last name is not enough - an dedicated equals could handle it
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
                // STORING the new guest in its domain
                guestService.createGuest(guest);
            });
        }

        booking.setId(UUID.randomUUID());
        storageService.schema.getBookings().add(booking);

        // STORE IT!
        storageService.store(storageService.schema.getBookings());
        log.info("Created booking: {}", booking);

        // SEND IT TO CONSUMER SERVICE
        // TODO serialize Booking using EclipseSerializer to send it to the Consumer Service

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
