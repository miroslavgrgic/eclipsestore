package hr.ogcs.eclipsestore.hotel.domain.guest;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.BookingEvent;
import hr.ogcs.eclipsestore.hotel.domain.booking.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component("GuestBookingEventListener")
@Slf4j
public class BookingEventListener implements ApplicationListener<BookingEvent> {

    @Autowired
    private BookingService bookingService;

    @Override
    public void onApplicationEvent(BookingEvent event) {
        bookingService.getBooking(((Booking) event.getSource()).getId())
                .ifPresent(this::sendMail);
    }

    private void sendMail(Booking booking) {
        // TODO send mail using mail provider
        log.info("Sent mail to guest: {}", booking.getGuests().getFirst());
    }

}