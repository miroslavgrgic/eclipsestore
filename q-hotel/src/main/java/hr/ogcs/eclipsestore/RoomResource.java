package hr.ogcs.eclipsestore;

import hr.ogcs.eclipsestore.model.Booking;
import hr.ogcs.eclipsestore.model.Room;
import hr.ogcs.eclipsestore.service.BookingService;
import hr.ogcs.eclipsestore.service.RoomService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/rooms")
public class RoomResource {

    @Inject
    private RoomService roomService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Room createRoom(Room room) {
        return roomService.createRoom(room);
    }

}
