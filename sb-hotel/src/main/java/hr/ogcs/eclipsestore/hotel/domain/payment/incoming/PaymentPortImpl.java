package hr.ogcs.eclipsestore.hotel.domain.payment.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.payment.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentPortImpl implements PaymentPort {

    @Autowired
    PaymentEventPublisher paymentEventPublisher;

    @Override
    public void processPayment(Booking booking) {
        callPaymentProvider(booking);
        paymentEventPublisher.publishPaymentEvent(booking);
    }

    private void callPaymentProvider(Booking booking) {
        // TODO implement some PaymentAdapter
        log.info("Called payment provider for Booking {}", booking);
    }

}
