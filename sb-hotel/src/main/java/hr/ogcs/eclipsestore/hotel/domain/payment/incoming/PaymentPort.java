package hr.ogcs.eclipsestore.hotel.domain.payment.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;

public interface PaymentPort {

    void processPayment(Booking booking);
}
