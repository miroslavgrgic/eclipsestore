package hr.ogcs.eclipsestore.hotel.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@ToString
public class Booking {

    @Builder.Default
    @Setter
    private UUID id = UUID.randomUUID();

    private List<Guest> guests;

    private Room room;

    private LocalDate date;

}
