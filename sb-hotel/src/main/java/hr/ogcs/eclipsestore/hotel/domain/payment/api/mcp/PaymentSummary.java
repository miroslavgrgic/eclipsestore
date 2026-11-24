package hr.ogcs.eclipsestore.hotel.domain.payment.api.mcp;

import hr.ogcs.eclipsestore.hotel.domain.payment.model.Payment;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public record PaymentSummary(UUID bookingId,
                             BigDecimal price,
                             String paymentProviderId,
                             Date paymentDate
                             ) {

    static PaymentSummary from(Payment payment) {
        return new PaymentSummary(
                payment.getBooking().getId(),
                payment.getBooking().getPrice(),
                payment.getPaymentProviderId(),
                payment.getPaymentDate());
    }
}
