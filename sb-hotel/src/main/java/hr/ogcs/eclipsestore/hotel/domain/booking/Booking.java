package hr.ogcs.eclipsestore.hotel.domain.booking;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import hr.ogcs.eclipsestore.hotel.domain.filter.Filterable;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.EQUALS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.GREATER_OR_EQUAL;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.IN;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.LESS_OR_EQUAL;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@ToString
public class Booking {

    @Builder.Default
    @Setter
    @Filterable
    private UUID id = UUID.randomUUID();

    private List<Guest> guests;

    @Setter
    private Room room;

    @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
    private LocalDate from;
    @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
    private LocalDate to;

    @Setter
    @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
    private BigDecimal price;

    @Builder.Default
    @Setter
    @Filterable({EQUALS, IN})
    private PaymentStatus paymentStatus = PaymentStatus.OPEN;

    public enum PaymentStatus {
        OPEN,
        PAID,
        CANCELLED
    }

    public boolean isValid() {
        final int MINIMUM_DAYS = 2;
        return switch (this) {
            case null -> false;
            case Booking b when b.getGuests().isEmpty() -> false;
            case Booking b when b.getRoom() == null -> false;
            case Booking b when b.getFrom().isAfter(b.getTo()) -> false;
            case Booking b when ChronoUnit.DAYS.between(b.getFrom(), b.getTo()) < MINIMUM_DAYS -> false;
            default -> true;
        };
    }

}
