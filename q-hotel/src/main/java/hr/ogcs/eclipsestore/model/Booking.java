package hr.ogcs.eclipsestore.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class Booking {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    private List<Guest> guests;

    private Room room;

    private LocalDate date;


    // TODO move logic to
//    public Optional<Booking> getBooking(UUID id) {
//        return bookings.stream().filter(booking -> booking.getId().equals(id)).findFirst();
//    }

}
