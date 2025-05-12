package hr.ogcs.eclipsestore.hotel.domain.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BookingEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishBookingEvent(final Booking booking) {
        BookingEvent bookingEvent = new BookingEvent(booking);
        applicationEventPublisher.publishEvent(bookingEvent);
    }

}