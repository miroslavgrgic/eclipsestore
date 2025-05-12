package hr.ogcs.eclipsestore.hotel.domain.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public sealed interface PricingPort permits PricingPortImpl {

    BigDecimal getPriceOfRoom(UUID roomId, LocalDate from, LocalDate to);

}
