package hr.ogcs.eclipsestore.hotel.domain.payment;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.BookingEvent;
import hr.ogcs.eclipsestore.hotel.domain.booking.incoming.BookingPort;
import hr.ogcs.eclipsestore.hotel.domain.payment.incoming.PaymentPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component("PaymentBookingEventListener")
@Slf4j
public class BookingEventListener implements ApplicationListener<BookingEvent> {

    @Autowired
    //private BookingService bookingService;
    private BookingPort bookingPort;
    @Autowired
    private PaymentPort paymentPort;

    @Override
    public void onApplicationEvent(BookingEvent event) {
        Booking booking = (Booking) event.getSource();

        paymentPort.processPayment(booking);
        bookingPort.getBooking((booking).getId())
                .ifPresent(b -> b.setPaymentStatus(Booking.PaymentStatus.PAID));

        log.info("Triggerd payment for booking: {} and set to PAID status", booking);
    }

}