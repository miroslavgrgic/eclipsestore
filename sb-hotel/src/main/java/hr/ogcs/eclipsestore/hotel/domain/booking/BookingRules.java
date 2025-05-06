package hr.ogcs.eclipsestore.hotel.domain.booking;

import java.time.temporal.ChronoUnit;

public class BookingRules {

    static final int MINIMUM_DAYS = 2;

    public static boolean isBookingValid(final Booking booking) {
        if (booking == null) return false;
        if (booking.getFrom().isAfter(booking.getTo())) return false;
        if (ChronoUnit.DAYS.between(booking.getFrom(), booking.getTo()) < MINIMUM_DAYS) return false;
        return true;
    }

}
