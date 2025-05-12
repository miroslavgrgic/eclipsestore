package hr.ogcs.eclipsestore.hotel.domain.booking.outgoing;

import hr.ogcs.eclipsestore.hotel.domain.pricing.incoming.PricingPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class PricingAdapter {

    final PricingPort pricingPort;

    public PricingAdapter(PricingPort pricingPort) {
        this.pricingPort = pricingPort;
    }

    public BigDecimal getPriceOfRoom(UUID id, LocalDate from, LocalDate to) {
        return pricingPort.getPriceOfRoom(id, from, to);
    }

}
