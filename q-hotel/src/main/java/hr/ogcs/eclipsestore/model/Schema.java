package hr.ogcs.eclipsestore.model;

import jakarta.inject.Singleton;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//@Singleton
public class Schema {

    @Getter
    private List<Booking> bookings = new ArrayList<>();
    @Getter
    private List<Room> rooms = new ArrayList<>();
    @Getter
    private List<Guest> guests = new ArrayList<>();

    public Optional<Booking> getBooking(UUID id) {
        return bookings.stream().filter(booking -> booking.getId().equals(id)).findFirst();
    }

    public Optional<Guest> searchGuest(String lastName) {
        return guests.stream()
                .filter(guest -> guest.getLastName().toLowerCase().contains(lastName))
                .findFirst();
    }

}
