package hr.ogcs.eclipsestore.hotel.domain.guest;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Address {
    private String street;
    private Integer postalCode;
    private String city;
    private String state;
}
