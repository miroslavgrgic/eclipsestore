package hr.ogcs.eclipsestore.service;

import hr.ogcs.eclipsestore.model.Room;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class RoomService {

    public List<Room> getAllRooms() {
        return StorageService.schema.getRooms();
    }

    public Room createRoom(Room room) {
        if (room.getId() == null) {
            room.setId(UUID.randomUUID());
        }
        StorageService.schema.getRooms().add(room);
        // STORE IT!
        StorageService.storageManager.store(StorageService.schema.getRooms());
        return room;
    }

}
