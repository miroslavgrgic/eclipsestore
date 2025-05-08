package hr.ogcs.eclipsestore.hotel.domain.booking;

import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import hr.ogcs.eclipsestore.hotel.domain.room.RoomPort;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import hr.ogcs.eclipsestore.hotel.domain.room.RoomPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RoomAdapter {

    final RoomPort roomPort;

    public RoomAdapter(RoomPort roomPort) {
        this.roomPort = roomPort;
    }

    public Optional<Room> findById(UUID id) {
        return roomPort.findById(id);
    }

}
