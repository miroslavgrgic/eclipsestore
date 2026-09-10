package hr.ogcs.eclipsestore.hotel.domain.guest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import hr.ogcs.eclipsestore.hotel.domain.filter.Filterable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.CONTAINS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.EQUALS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.GREATER_OR_EQUAL;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.LESS_OR_EQUAL;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@ToString
public class Guest {

    @Builder.Default
    @Setter
    @Filterable
    private UUID id = UUID.randomUUID();

    @Filterable({EQUALS, CONTAINS})
    private String firstName;
    @Filterable({EQUALS, CONTAINS})
    private String lastName;

    @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
    private int age;

    private Address address;

    public boolean isAllowedToConsumeAlcohol() {
        return age >= 18;
    }

    public boolean isChildren() {
        return age < 13;
    }

    public boolean hasValidData() {
        return id != null && firstName != null && lastName != null && age > -1;
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    public boolean isAddressValid() {
        // TODO implement!
        return true;
    }

}
