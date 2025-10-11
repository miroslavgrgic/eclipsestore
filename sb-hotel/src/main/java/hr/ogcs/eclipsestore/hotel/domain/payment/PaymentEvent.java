package hr.ogcs.eclipsestore.hotel.domain.payment;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class PaymentEvent extends ApplicationEvent {

    @Getter
    private final String message;
    @Getter
    private final UUID bookingId;

    public PaymentEvent(Object source, UUID bookingId, String message) {
        super(source);
        this.bookingId = bookingId;
        this.message = message;
    }

}
