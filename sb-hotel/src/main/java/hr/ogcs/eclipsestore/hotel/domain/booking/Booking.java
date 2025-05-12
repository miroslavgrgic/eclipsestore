package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@ToString
public class Booking {

    @Builder.Default
    @Setter
    private UUID id = UUID.randomUUID();

    private List<Guest> guests;

    private Room room;

    private LocalDate from;
    private LocalDate to;

    @Setter
    private BigDecimal price;

    @Builder.Default
    @Setter
    private PaymentStatus paymentStatus = PaymentStatus.OPEN;

    public enum PaymentStatus {
        OPEN,
        PAID,
        CANCELLED
    }

}
