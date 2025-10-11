package hr.ogcs.eclipsestore.hotel.domain.booking;

import java.time.temporal.ChronoUnit;

public class BookingRules {

    private static final int MINIMUM_DAYS = 2;

    public static boolean isBookingValid(Booking booking) {
        return switch (booking) {
            case null -> false;
            case Booking b when b.getGuests().isEmpty() -> false;
            case Booking b when b.getRoom() == null -> false;
            case Booking b when b.getFrom().isAfter(b.getTo()) -> false;
            case Booking b when ChronoUnit.DAYS.between(b.getFrom(), b.getTo()) < MINIMUM_DAYS -> false;
            default -> true;
        };
    }

}
