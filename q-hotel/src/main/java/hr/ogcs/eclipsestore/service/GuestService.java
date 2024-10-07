package hr.ogcs.eclipsestore.service;

import hr.ogcs.eclipsestore.model.Guest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class GuestService {

    public List<Guest> getAllGuests() {
        return StorageService.schema.getGuests();
    }

    public Guest addGuest(Guest guest) {
        if (guest.getId() == null) {
            guest.setId(UUID.randomUUID());
        }
        StorageService.schema.getGuests().add(guest);
        // STORE IT!
        StorageService.storageManager.store(StorageService.schema.getGuests());
        return guest;
    }

    public boolean checkGuests(List<Guest> guests) {
        guests.stream().findFirst().orElseThrow(IllegalArgumentException::new);
        return true;
    }
}
