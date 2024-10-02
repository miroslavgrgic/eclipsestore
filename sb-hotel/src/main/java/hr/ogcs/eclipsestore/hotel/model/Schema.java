package hr.ogcs.eclipsestore.hotel.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Schema {

    public Schema() {}

    private List<Booking> bookings = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private List<Guest> guests = new ArrayList<>();

}
