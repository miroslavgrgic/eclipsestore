package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Room;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RoomService {

    // member
    private StorageService storageService;

    // constructor
    public RoomService(StorageService storageService) {
        this.storageService = storageService;
    }

    // methods
    public List<Room> getAllRooms() {
        return storageService.schema.getRooms();
    }

    public Room createRoom(Room room) {
        if (room.getId() == null) {
            room.setId(UUID.randomUUID());
        }

        // add to existing list
        storageService.schema.getRooms().add(room);

        storageService.store(storageService.schema.getRooms());
        return room;
    }

    public void deleteRoomByID(String id) {
        Optional<Room> room = storageService.schema.getRooms().stream()
                .filter(item -> item.getId().toString().equals(id))
                .findFirst();

        if (room.isEmpty()) {
            throw new IllegalArgumentException("Trying to delete entry that does not exist!");
        } else {
            storageService.schema.getRooms().remove(room.get());
            storageService.storageManager.store(storageService.schema.getRooms());
            log.info("Deleted Room with ID {}", id);
        }
    }

    public Optional<Room> findById(UUID id) {
        return storageService.schema.getRooms().stream()
                .filter(room -> room.getId().equals(id))
                .findFirst();
    }

    public void extendBeds(Room room, int width, int length) {
        room.addAdditionalBedSize(width, length);
    }

}
