package hr.ogcs.eclipsestore.hotel.domain.payment.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.payment.Payment;
import hr.ogcs.eclipsestore.hotel.domain.payment.PaymentEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PaymentPortImpl implements PaymentPort {

    @Autowired
    PaymentEventPublisher paymentEventPublisher;
    @Autowired
    PaymentService paymentService;

    @Override
    public Map<UUID, Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @Override
    public void processPayment(Booking booking) {
        callPaymentProvider(booking);
        paymentEventPublisher.publishPaymentEvent(booking);
    }

    private void callPaymentProvider(Booking booking) {
        // TODO implement some PaymentAdapter
        paymentService.auditPayment(booking);
        log.info("Called payment provider for Booking {}", booking);
    }

}
