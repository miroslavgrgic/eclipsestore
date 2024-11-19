package hr.ogcs.eclipsestore.hotel.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Builder.Default
    private Map<Integer, String> combinations = Map.of(
        1, "use case 1",
        2, "use case 2",
        3, "use case 3"
    );

    @Builder.Default
    private final List<String> bedSizes = new ArrayList<>(0);

    @Builder.Default
    private State state = State.FREE;

    private LocalDate availableSince;

    public enum State {
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
