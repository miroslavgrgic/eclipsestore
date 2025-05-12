package hr.ogcs.eclipsestore.hotel.domain.booking.outgoing;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DatabaseAdapter {

    final StorageService storageService;

    public DatabaseAdapter(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Booking> getBookings() {
        return storageService.hotel.getBookings();
    }

    public Optional<Booking> getBooking(UUID id) {
        return storageService.hotel.getBookings().stream().filter(booking -> booking.getId().equals(id)).findFirst();
    }

    public void save(Booking booking) {
        // entity needs a unique key
        booking.setId(UUID.randomUUID());

        storageService.hotel.getBookings().add(booking);
        // STORE IT!
        storageService.store(storageService.hotel.getBookings());
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
        }

    }
}
