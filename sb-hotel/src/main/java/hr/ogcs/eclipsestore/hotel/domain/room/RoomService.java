package hr.ogcs.eclipsestore.hotel.domain.room;

import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RoomService {

    private final StorageService storageService;

    public RoomService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Room> getAllRooms() {
        return storageService.hotel.getRooms();
    }

    public Room createRoom(Room room) {
        if (room.getId() == null) {
            room.setId(UUID.randomUUID());
        }

        // add to existing rooms list
        storageService.hotel.getRooms().add(room);
        // store the rooms list
        storageService.store(storageService.hotel.getRooms());
        return room;
    }

    public void deleteRoomByID(UUID id) {
        Optional<Room> room = storageService.hotel.getRooms().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();

        if (room.isEmpty()) {
            throw new IllegalArgumentException("Trying to delete entry that does not exist!");
        } else {
            storageService.hotel.getRooms().remove(room.get());
            storageService.storageManager.store(storageService.hotel.getRooms());
            log.info("Deleted Room with ID {}", id);
        }
    }

    public Optional<Room> findById(UUID id) {
        return storageService.hotel.getRooms().stream()
                .filter(room -> room.getId().equals(id))
                .findFirst();
    }

    public List<Room> findBySize(int minimumSqm) {
        return storageService.hotel.getRooms().stream()
                .filter(room -> room.getSqm() >= minimumSqm)
                .toList();
    }

    public List<Room> findByPrice(int min, int max) {
        return storageService.hotel.getRooms().stream()
                .filter(room -> room.getPrice().intValue() >= min && room.getPrice().intValue() <= max)
                .toList();
    }

    public List<Room> findByNumberOfGuest(int amount) {
        return storageService.hotel.getRooms().stream()
                .filter(room -> room.maxNumberOfGuests() > amount)
                .toList();
    }

    public void extendBeds(Room room, int width, int length) {
        room.addAdditionalBedSize(width, length);
    }

}
