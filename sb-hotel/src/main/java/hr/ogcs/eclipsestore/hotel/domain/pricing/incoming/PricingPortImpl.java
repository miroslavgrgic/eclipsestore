package hr.ogcs.eclipsestore.hotel.domain.pricing.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.outgoing.RoomAdapter;
import hr.ogcs.eclipsestore.hotel.domain.pricing.Price;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public final class PricingPortImpl implements PricingPort {

    private final RoomAdapter roomAdapter;

    public PricingPortImpl(RoomAdapter roomAdapter) {
        this.roomAdapter = roomAdapter;
    }

    @Override
    public BigDecimal getPriceOfRoom(UUID roomId, LocalDate from, LocalDate to) {
        Optional<Room> room = roomAdapter.findById(roomId);
        if (room.isEmpty()) {
            throw new IllegalArgumentException("Room " + roomId + " not found");
        }

        return switch (from) {
            case LocalDate f when isDuringXmasTime(f) -> room.get().getDefaultPrice().multiply(Price.getXmasSurcharge());
            default -> room.get().getDefaultPrice();
        };
    }

    private boolean isDuringXmasTime(LocalDate from) {
        LocalDate dayBeforeXmas = LocalDate.of(LocalDate.now().getYear(), 12, 23);
        LocalDate dayAfterXmas = LocalDate.of(LocalDate.now().getYear(), 12, 27);
        return from.isAfter(dayBeforeXmas) && from.isBefore(dayAfterXmas);
    }

}
