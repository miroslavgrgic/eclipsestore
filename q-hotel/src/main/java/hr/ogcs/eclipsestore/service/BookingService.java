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

    @Inject
    private PersistenceService persistenceService;

    @Inject
    private RoomService roomService;

    public List<Booking> getAllBookings() {
        return persistenceService.getSchema().getBookings();
    }

    public Booking createBooking(Booking booking) {
        // Check if room exists
        roomService.getAllRooms().stream()
                .filter(room -> room.getId().equals(booking.getRoom().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + booking.getRoom().getId() + " does not exist"));

        booking.getGuests().stream().forEach(guest -> guest.setId(UUID.randomUUID()));
        persistenceService.getSchema().getBookings().add(booking);
        persistenceService.getStorageManager().store(persistenceService.getSchema().getBookings());
        return booking;
    }

}
