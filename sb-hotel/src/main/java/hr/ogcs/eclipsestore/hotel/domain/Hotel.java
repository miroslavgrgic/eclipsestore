package hr.ogcs.eclipsestore.hotel.domain;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.*;

@Getter
public class Hotel {

    private final String name;
    @Accessors(fluent = true)
    private final boolean acceptsCreditCards;

    public Hotel(String name, boolean acceptsCreditCards) {
        this.name = name;
        this.acceptsCreditCards = acceptsCreditCards;
    }

    // Our Domain Model
    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<Guest> guests = new ArrayList<>();

    // Still Domain model, but enriching by technical key for easier access
    private final Map<UUID, Object> someObjects = new HashMap<>();

    public boolean isHandicapFriendlyHotel() {
        return rooms.stream()
                .filter(Room::canBeUsedWithHandicaps)
                .count() > 2;
    }

    // TODO implement some more Hotel domain related methods

}
