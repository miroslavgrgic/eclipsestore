package hr.ogcs.eclipsestore.hotel.domain.booking;

import org.springframework.context.ApplicationEvent;

public class BookingEvent extends ApplicationEvent {

    public BookingEvent(Object source) {
        super(source);
    }

    public Booking getBooking() {
        return (Booking) this.source;
    }

}
