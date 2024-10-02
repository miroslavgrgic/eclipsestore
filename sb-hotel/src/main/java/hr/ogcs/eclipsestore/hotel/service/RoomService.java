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

    private StorageService storageService;

    public RoomService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Room> getAllRooms() {
        return storageService.schema.getRooms();
    }

    public Room createRoom(Room room) {
        var newRoom = Room.builder()
                .id(UUID.randomUUID())
                .name(room.getName())
                .sqm(room.getSqm())
                .state(room.getState())
                .bedSizes(room.getBedSizes())
                .availableSince(room.getAvailableSince())
                .canBeUsedWithHandicaps(room.isCanBeUsedWithHandicaps())
                .build();

        // add to existing list
        storageService.schema.getRooms().add(newRoom);

        storageService.store(storageService.schema.getRooms());
        return newRoom;
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

}
