package hr.ogcs.eclipsestore.hotel.domain.pricing;

import hr.ogcs.eclipsestore.hotel.domain.booking.RoomAdapter;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public final class PricingPortImpl implements PricingPort {

    @Autowired
    private RoomAdapter roomAdapter;

    @Override
    public BigDecimal getPriceOfRoom(UUID roomId, LocalDate from, LocalDate to) {
        Optional<Room> room = roomAdapter.findById(roomId);
        if (room.isEmpty()) {
            throw new IllegalArgumentException("Room " + roomId + " not found");
        }

        return switch (from) {
            case LocalDate f when isDuringXmasTime(f) -> room.get().getDefaultPrice().multiply(BigDecimal.valueOf(1.2));
            default -> room.get().getDefaultPrice();
        };
    }

    private boolean isDuringXmasTime(LocalDate from) {
        LocalDate dayBeforeXmas = LocalDate.of(LocalDate.now().getYear(), 12, 23);
        LocalDate dayAfterXmas = LocalDate.of(LocalDate.now().getYear(), 12, 27);
        return from.isAfter(dayBeforeXmas) && from.isBefore(dayAfterXmas);
    }

}
