package hr.ogcs.eclipsestore.service;

import hr.ogcs.eclipsestore.model.Booking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
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
        // Check if room exists
        roomService.getAllRooms().stream()
                .filter(room -> room.getId().equals(booking.getRoom().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + booking.getRoom().getId() + " does not exist"));

        // Check if guest exists
        guestService.checkGuests(booking.getGuests());

        booking.getGuests().stream().forEach(guest -> guest.setId(UUID.randomUUID()));
        storageService.schema.getBookings().add(booking);
        // STORE IT!
        storageService.storageManager.store(storageService.schema.getBookings());
        return booking;
    }

}
