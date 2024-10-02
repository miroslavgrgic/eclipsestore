package hr.ogcs.eclipsestore.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Schema {

    // Our Domain Model
    private List<Booking> bookings = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private List<Guest> guests = new ArrayList<>();

    // TODO implement some Domain related methods

}
