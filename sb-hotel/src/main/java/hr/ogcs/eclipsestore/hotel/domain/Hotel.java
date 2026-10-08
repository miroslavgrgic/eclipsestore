package hr.ogcs.eclipsestore.hotel.domain;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.payment.model.Payment;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.store.gigamap.types.GigaMap;

import java.util.*;
import java.util.stream.StreamSupport;

@Getter
public class Hotel {

    // Our Domain Model
    // this is a "has" relation in Graph theorem
    private final List<Room> rooms;
    private final List<Booking> bookings;
    // GigaMap is the source of truth for guests: it carries the JVector similarity index,
    // which is only kept in sync for entities added/updated/removed through the map itself.
    private final GigaMap<Guest> guests;
    // Still Domain model, but enriching by technical key for easier access
    // Lazily loaded: the payments map is only fetched from storage on first access
    private final Lazy<Map<UUID, Payment>> payments;

    private final String name;
    @Accessors(fluent = true)
    private final boolean acceptsCreditCards;

    // Constructors
    public Hotel(String name, boolean acceptsCreditCards) {
        this(name, acceptsCreditCards, new ArrayList<>(), List.of(), new ArrayList<>(), new HashMap<>());
    }

    // Builds an in-memory, non-persisted view of the hotel restricted to the given child
    // entities, e.g. for returning filtered results without mutating or storing anything.
    public Hotel(String name, boolean acceptsCreditCards, List<Room> rooms, List<Guest> guests,
                 List<Booking> bookings, Map<UUID, Payment> payments) {
        this.name = name;
        this.acceptsCreditCards = acceptsCreditCards;
        this.rooms = rooms;
        this.guests = GigaMap.New();
        this.guests.addAll(guests);
        this.bookings = bookings;
        this.payments = Lazy.Reference(payments);
    }


    public List<Guest> getGuests() {
        return StreamSupport.stream(guests.spliterator(), false).toList();
    }
    public GigaMap<Guest> getGuestMap() {
        return guests;
    }
    public Map<UUID, Payment> getPayments() {
        return payments.get();
    }

    // Domain logic
    public boolean isHandicapFriendlyHotel() {
        return rooms.stream()
                .filter(Room::canBeUsedWithHandicaps)
                .count() > 2;
    }
    // TODO implement some more Hotel domain related methods

}
