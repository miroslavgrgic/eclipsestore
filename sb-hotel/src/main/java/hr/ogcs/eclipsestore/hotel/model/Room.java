package hr.ogcs.eclipsestore.hotel.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Builder
@Getter
@ToString
public class Room {

    @Builder.Default
    @Setter
    private UUID id = UUID.randomUUID();

    private String name;

    private BigDecimal price;

    private int sqm;

    private boolean canBeUsedWithHandicaps;

//    private Map<Object, Object> combinations = Map.of(
//
//    );

    @Builder.Default
    private final List<String> bedSizes = new ArrayList<>(0);

    @Builder.Default
    private State state = State.FREE;

    private LocalDate availableSince;

    private enum State {
        FREE,
        BLOCKED
    }

    public void addAdditionalBedSize(int widthInCm, int lengthInCm) {
        if (widthInCm > lengthInCm) {
            throw new IllegalArgumentException("Width and Length mismatch");
        }
        bedSizes.add(String.format("%d x %d", widthInCm, lengthInCm));
    }

    public int maxNumberOfGuests() {
        return sqm / 20;
    }

}
