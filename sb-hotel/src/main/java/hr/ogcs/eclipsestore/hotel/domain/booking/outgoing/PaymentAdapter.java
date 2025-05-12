package hr.ogcs.eclipsestore.hotel.domain.booking.outgoing;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.payment.incoming.PaymentPort;
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
