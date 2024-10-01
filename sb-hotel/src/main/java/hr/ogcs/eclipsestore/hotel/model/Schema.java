package hr.ogcs.eclipsestore.hotel.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Schema {

    public Schema() {}

    @Getter
    private List<Booking> bookings = new ArrayList<>();
    @Getter
    private List<Room> rooms = new ArrayList<>();
    @Getter
    private List<Guest> guests = new ArrayList<>();

}
