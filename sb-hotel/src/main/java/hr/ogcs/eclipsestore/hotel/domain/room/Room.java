package hr.ogcs.eclipsestore.hotel.domain.room;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import hr.ogcs.eclipsestore.hotel.domain.filter.Filterable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.CONTAINS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.EQUALS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.GREATER_OR_EQUAL;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.IN;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.LESS_OR_EQUAL;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@ToString
public class Room {

    @Builder.Default
    @Setter
    @Filterable
    private UUID id = UUID.randomUUID();

    @Filterable({EQUALS, CONTAINS})
    private String name;

    @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
    private BigDecimal defaultPrice;

    @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
    private int sqm;

    @Accessors(fluent = true)
    @Filterable
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
    @Filterable({EQUALS})
    private State state = State.FREE;

    //@Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
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
