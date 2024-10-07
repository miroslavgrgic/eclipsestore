package hr.ogcs.eclipsestore.hotel.repository;

import hr.ogcs.eclipsestore.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

}
