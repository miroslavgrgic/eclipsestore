package hr.ogcs.eclipsestore.hotel.domain.room.incoming;

import hr.ogcs.eclipsestore.hotel.domain.room.Room;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public sealed interface RoomPort permits RoomPortImpl {

    Optional<Room> findById(UUID id);
    List<Room> findBySize(int minimumSqm);
    List<Room> findByPrice(int min, int max);
    List<Room> findByNumberOfGuest(int amount);

}
