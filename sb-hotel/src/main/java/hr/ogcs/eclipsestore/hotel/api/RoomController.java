package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import hr.ogcs.eclipsestore.hotel.domain.room.incoming.RoomPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/rooms")
public class RoomController {

    private final RoomPort roomPort;

    public RoomController(RoomPort roomPort) {
        this.roomPort = roomPort;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room newRoom = roomPort.createRoom(room);
        return ResponseEntity.status(HttpStatus.OK).body(newRoom);
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomPort.getAllRooms();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<UUID> deleteRoom(@PathVariable(value = "id") UUID id) {
        roomPort.deleteRoomByID(id);
        return ResponseEntity.accepted().body(id);
    }
}
