package hr.ogcs.eclipsestore.hotel.repository;

import hr.ogcs.eclipsestore.hotel.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuestRepository extends JpaRepository<Guest, UUID> {
    List<Guest> findByLastName(String lastName);
}
