package hr.ogcs.eclipsestore.hotel.domain.room;

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
    public Optional<Room> findById(UUID id) {
        return roomService.findById(id);
    }

    @Override
    public List<Room> findBySize(int minimumSqm) {
        return roomService.findBySize(minimumSqm);
    }

    @Override
    public List<Room> findByPrice(int min, int max) {
        return roomService.findByPrice(min, max);
    }

    @Override
    public List<Room> findByNumberOfGuest(int amount) {
        return roomService.findByNumberOfGuest(amount);
    }

}
