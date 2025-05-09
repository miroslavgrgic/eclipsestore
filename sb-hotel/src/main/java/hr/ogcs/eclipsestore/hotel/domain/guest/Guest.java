package hr.ogcs.eclipsestore.hotel.domain.guest;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Builder
@Getter
@ToString
public class Guest {

    @Builder.Default
    @Setter
    private UUID id = UUID.randomUUID();

    private String firstName;
    private String lastName;

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
}
