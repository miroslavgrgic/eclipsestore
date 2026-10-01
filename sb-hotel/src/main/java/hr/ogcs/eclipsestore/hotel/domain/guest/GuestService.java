package hr.ogcs.eclipsestore.hotel.domain.guest;

import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GuestService {

    private final StorageService storageService;

    public GuestService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Guest> getAllGuests() {
        return storageService.hotel.getGuests();
    }

    public Guest createGuest(Guest guest) {
        if (guest.getId() == null) {
            guest.setId(UUID.randomUUID());
        }

        if (!guest.hasValidData()) {
            throw new IllegalArgumentException("Invalid guest");
        }

        storageService.hotel.getGuestMap().add(guest);
        storageService.storageManager.store(storageService.hotel.getGuestMap());
        return guest;
    }

    public Optional<Guest> findById(UUID id) {
        return storageService.hotel.getGuests().stream()
                .filter(guest -> guest.getId().equals(id))
                .findFirst();
    }

    public Optional<Guest> findByLastname(String lastName) {
        return storageService.hotel.getGuests().stream()
                .filter(guest -> guest.getLastName().equalsIgnoreCase(lastName))
                .findFirst();
    }

    public Guest updateGuest(Guest updatedGuest) {
        if (!updatedGuest.hasValidData()) {
            throw new IllegalArgumentException("Invalid guest");
        }

        Guest existing = findById(updatedGuest.getId())
                .orElseThrow(() -> new IllegalArgumentException("Trying to update entry that does not exist!"));

        storageService.hotel.getGuestMap().replace(existing, updatedGuest);
        storageService.storageManager.store(storageService.hotel.getGuestMap());
        log.info("Updated Guest with ID {}", updatedGuest.getId());
        return updatedGuest;
    }

    public void deleteGuestByID(UUID id) {
        Optional<Guest> guest = findById(id);

        if (guest.isEmpty()) {
            throw new IllegalArgumentException("Trying to delete entry that does not exist!");
        } else {
            storageService.hotel.getGuestMap().remove(guest.get());
            storageService.storageManager.store(storageService.hotel.getGuestMap());
            log.info("Deleted Guest with ID {}", id);
        }
    }

    public void createGuests(List<Guest> guests) {
        storageService.hotel.getGuestMap().addAll(guests);
        storageService.storageManager.store(storageService.hotel.getGuestMap());
    }

}