package hr.ogcs.eclipsestore.hotel.domain.pricing;

import hr.ogcs.eclipsestore.hotel.domain.booking.RoomAdapter;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class PricingPortImpl implements PricingPort {

    @Autowired
    private RoomAdapter roomAdapter;

    @Override
    public BigDecimal getPriceOfRoom(UUID roomId, Date from, Date to) {
        Optional<Room> room = roomAdapter.findById(roomId);
        if (room.isEmpty()) {
            throw new IllegalArgumentException("Room " + roomId + " not found");
        }
        return switch (from) {
            // TODO add some cases
            case Date f when f.after(Date.from(Instant.from(LocalDate.now().plusDays(1)))) -> BigDecimal.valueOf(0);
            default -> room.get().getDefaultPrice();
        };
    }

}
