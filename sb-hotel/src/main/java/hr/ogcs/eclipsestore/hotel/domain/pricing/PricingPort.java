package hr.ogcs.eclipsestore.hotel.domain.pricing;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

public interface PricingPort {

    BigDecimal getPriceOfRoom(UUID roomId, Date from, Date to);

}
