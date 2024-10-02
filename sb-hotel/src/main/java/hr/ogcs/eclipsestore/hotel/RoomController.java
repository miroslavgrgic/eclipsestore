package hr.ogcs.eclipsestore.hotel;

import hr.ogcs.eclipsestore.hotel.model.Room;
import hr.ogcs.eclipsestore.hotel.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity deleteRoom(@PathVariable(value = "id") String id) {
        roomService.deleteRoomByID(id);
        return ResponseEntity.accepted().build();
    }
}
