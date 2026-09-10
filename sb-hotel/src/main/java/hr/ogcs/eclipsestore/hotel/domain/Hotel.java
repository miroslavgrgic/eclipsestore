package hr.ogcs.eclipsestore.hotel.domain;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.payment.model.Payment;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.serializer.reference.Lazy;

import java.util.*;

@Getter
public class Hotel {

    private final String name;
    @Accessors(fluent = true)
    private final boolean acceptsCreditCards;

    public Hotel(String name, boolean acceptsCreditCards) {
        this(name, acceptsCreditCards, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>());
    }

    // Builds an in-memory, non-persisted view of the hotel restricted to the given child
    // entities, e.g. for returning filtered results without mutating or storing anything.
    public Hotel(String name, boolean acceptsCreditCards, List<Room> rooms, List<Guest> guests,
                 List<Booking> bookings, Map<UUID, Payment> payments) {
        this.name = name;
        this.acceptsCreditCards = acceptsCreditCards;
        this.rooms = rooms;
        this.guests = guests;
        this.bookings = bookings;
        this.payments = Lazy.Reference(payments);
    }

    // Our Domain Model
    // this is a "has" relation in Graph theorem
    private final List<Room> rooms;
    // TODO create annotation that describes another graph relation type, like "belongs to"
    private final List<Guest> guests;
    private final List<Booking> bookings;

    // Still Domain model, but enriching by technical key for easier access
    // Lazily loaded: the payments map is only fetched from storage on first access
    @Getter(AccessLevel.NONE)
    private final Lazy<Map<UUID, Payment>> payments;

    public Map<UUID, Payment> getPayments() {
        return payments.get();
    }

    public boolean isHandicapFriendlyHotel() {
        return rooms.stream()
                .filter(Room::canBeUsedWithHandicaps)
                .count() > 2;
    }

    // TODO implement some more Hotel domain related methods

}
