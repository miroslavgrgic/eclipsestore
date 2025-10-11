package hr.ogcs.eclipsestore.hotel.domain.pricing;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;

public class Price {

    private BigDecimal price;
    @Getter
    private Currency currency;
    private BigDecimal discount;

    public boolean isPriceValid() {
        return price.compareTo(BigDecimal.ZERO) > 0 && price.compareTo(discount) > 0;
    }

    public BigDecimal getPrice() {
        return price.subtract(discount);
    }

    public static BigDecimal getXmasSurcharge() {
        return BigDecimal.valueOf(1.2);
    }

}
