package hr.ogcs.eclipsestore.hotel.domain.room.incoming;

import hr.ogcs.eclipsestore.hotel.domain.room.Room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public sealed interface RoomPort permits RoomPortImpl {

    List<Room> getAllRooms();
    Optional<Room> findById(UUID id);
    List<Room> findBySize(int minimumSqm);
    List<Room> findByPriceRange(int min, int max);
    List<Room> findByNumberOfGuest(int amount);

    Room createRoom(Room room);
    void deleteRoomByID(UUID id);
}
