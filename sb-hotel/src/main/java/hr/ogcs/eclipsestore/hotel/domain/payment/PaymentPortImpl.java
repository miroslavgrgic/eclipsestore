package hr.ogcs.eclipsestore.hotel.domain.payment;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.pricing.Price;
import org.springframework.stereotype.Component;

@Component
public class PaymentPortImpl implements PaymentPort {
    @Override
    public void processPayment(Booking booking, Price price) {
        if (price.getPrice().compareTo(booking.getRoom().getDefaultPrice()) > 0) {
            // TODO write here sth for the conference
        }
    }

    private void callPaymentProvider(Booking booking) {
        // TODO implement
    }
}
