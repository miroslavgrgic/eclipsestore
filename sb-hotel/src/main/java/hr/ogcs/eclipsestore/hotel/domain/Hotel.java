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
        this.name = name;
        this.acceptsCreditCards = acceptsCreditCards;
    }

    // Our Domain Model
    private final List<Room> rooms = new ArrayList<>();
    private final List<Guest> guests = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    // Still Domain model, but enriching by technical key for easier access
    // Lazily loaded: the payments map is only fetched from storage on first access
    @Getter(AccessLevel.NONE)
    private final Lazy<Map<UUID, Payment>> payments = Lazy.Reference(new HashMap<>());

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
