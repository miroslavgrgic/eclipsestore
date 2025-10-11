package hr.ogcs.eclipsestore.hotel.domain.payment.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.payment.Payment;

import java.util.Map;
import java.util.UUID;

public interface PaymentPort {

    Map<UUID, Payment> getAllPayments();
    void processPayment(Booking booking);

}
