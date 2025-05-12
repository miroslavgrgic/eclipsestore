package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.payment.PaymentPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentAdapter {

    @Autowired
    private PaymentPort paymentPort;

    public void triggerPayment(Booking booking) {
        paymentPort.processPayment(booking);
    }

}
