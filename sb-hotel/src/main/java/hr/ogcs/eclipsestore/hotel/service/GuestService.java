package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Guest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GuestService {

    private StorageService storageService;

    public GuestService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Guest> getAllGuests() {
        return storageService.schema.getGuests();
    }

    public Guest createGuest(Guest guest) {
        if (guest.getId() == null) {
            guest.setId(UUID.randomUUID());
        }

        storageService.schema.getGuests().add(guest);
        storageService.store(storageService.schema.getGuests());
        return guest;
    }

    public Optional<Guest> findByLastname(String lastName) {
        return storageService.schema.getGuests().stream()
                .filter(guest -> guest.getLastName().equalsIgnoreCase(lastName))
                .findFirst();
    }

    public void deleteGuestByID(String id) {
        Optional<Guest> guest = storageService.schema.getGuests().stream()
                .filter(item -> item.getId().toString().equals(id))
                .findFirst();

        if (guest.isEmpty()) {
            throw new IllegalArgumentException("Trying to delete entry that does not exist!");
        } else {
            storageService.schema.getGuests().remove(guest.get());
            storageService.storageManager.store(storageService.schema.getGuests());
            log.info("Deleted Guest with ID {}", id);
        }
    }

    public void createGuests(List<Guest> guests) {
        storageService.schema.getGuests().addAll(guests);
        storageService.storageManager.store(storageService.schema.getGuests());
    }
}