package hr.ogcs.eclipsestore.hotel.model;

import lombok.Getter;

import java.util.*;

@Getter
public class Schema {

    // Our Domain Model
    private List<Booking> bookings = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private List<Guest> guests = new ArrayList<>();

    // Still Domain model, but enriching by technical key for easier access
    private Map<UUID, FooBar> fooBars = new HashMap<>();

    // Domain related validations
    public boolean isBookingValid(Booking booking) {
        return !booking.getGuests().isEmpty();
    }

    public boolean isHandicapFriendlyHotel() {
        return rooms.stream()
                .filter(room -> room.isCanBeUsedWithHandicaps())
                .toList()
                .size() > 2;
    }

    // TODO implement some more Domain related methods

}
