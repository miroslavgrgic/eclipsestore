package hr.ogcs.eclipsestore.hotel.domain.payment;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishPaymentEvent(final Booking booking) {
        PaymentEvent paymentEvent = new PaymentEvent(booking, booking.getId(), "Booking successfully paid.");
        applicationEventPublisher.publishEvent(paymentEvent);
    }

}