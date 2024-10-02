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

    private StorageService storageService;

    public RoomService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Room> getAllRooms() {
        return storageService.schema.getRooms();
    }

    public Room createRoom(Room room) {
        room.setId(UUID.randomUUID());
        storageService.schema.getRooms().add(room);
        // STORE IT!
        storageService.storageManager.store(storageService.schema.getRooms());
        return room;
    }
}
