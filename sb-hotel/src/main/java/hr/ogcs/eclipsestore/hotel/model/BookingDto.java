package hr.ogcs.eclipsestore.hotel.model;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class BookingDto implements Serializable {

    Booking booking;
    List<Guest> guests;
    Room room;

    LocalDate date;
}