package hr.ogcs.eclipsestore.service;

import hr.ogcs.eclipsestore.model.Booking;
import hr.ogcs.eclipsestore.model.Room;
import hr.ogcs.eclipsestore.model.Schema;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class RoomService {

    @Inject
    private PersistenceService persistenceService;

    public List<Room> getAllRooms() {
        return persistenceService.getSchema().getRooms();
    }

    public Room createRoom(Room room) {
        room.setId(UUID.randomUUID());
        persistenceService.getSchema().getRooms().add(room);
        persistenceService.getStorageManager().store(room);
        return room;
    }
}
