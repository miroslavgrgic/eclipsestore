package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.payment.PaymentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener implements ApplicationListener<PaymentEvent> {

    @Autowired
    private BookingService bookingService;

    @Override
    public void onApplicationEvent(PaymentEvent event) {
        bookingService.getBooking(event.getBookingId())
                .ifPresent(booking -> booking.setPaymentStatus(Booking.PaymentStatus.PAID));
    }

}