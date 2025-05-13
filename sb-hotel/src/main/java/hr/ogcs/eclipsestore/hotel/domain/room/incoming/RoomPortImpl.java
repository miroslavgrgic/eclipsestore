package hr.ogcs.eclipsestore.hotel.domain.room.incoming;

import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import hr.ogcs.eclipsestore.hotel.domain.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public final class RoomPortImpl implements RoomPort {

    @Autowired
    RoomService roomService;

    @Override
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @Override
    public Optional<Room> findById(UUID id) {
        return roomService.findById(id);
    }

    @Override
    public List<Room> findBySize(int minimumSqm) {
        return roomService.findBySize(minimumSqm);
    }

    @Override
    public List<Room> findByPriceRange(int min, int max) {
        return roomService.findByPrice(min, max);
    }

    @Override
    public List<Room> findByNumberOfGuest(int amount) {
        return roomService.findByNumberOfGuest(amount);
    }

    @Override
    public Room createRoom(Room room) {
        return roomService.createRoom(room);
    }

    @Override
    public void deleteRoomByID(UUID id) {
        roomService.deleteRoomByID(id);
    }

}
