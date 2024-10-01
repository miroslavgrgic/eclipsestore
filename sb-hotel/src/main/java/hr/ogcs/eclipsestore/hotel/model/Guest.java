package hr.ogcs.eclipsestore.hotel.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
public class Guest {

    @Builder.Default
    @Setter
    private UUID id = UUID.randomUUID();

    private String firstName;
    private String lastName;

    private int age;

    public boolean isAllowedToConsumeAlcohol() {
        return age >= 18;
    }

    public boolean isChildren() {
        return age < 13;
    }

    private Address address;
}
