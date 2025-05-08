package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import hr.ogcs.eclipsestore.hotel.domain.room.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room newRoom = roomService.createRoom(room);
        return ResponseEntity.status(HttpStatus.OK).body(newRoom);
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<UUID> deleteRoom(@PathVariable(value = "id") UUID id) {
        roomService.deleteRoomByID(id);
        return ResponseEntity.accepted().body(id);
    }
}
