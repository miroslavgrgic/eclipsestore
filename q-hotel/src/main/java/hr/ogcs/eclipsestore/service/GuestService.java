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

    private StorageService storageService;

    public GuestService(StorageService storageService) {
        this.storageService = storageService;
    }

    public List<Guest> getAllGuests() {
        return storageService.schema.getGuests();
    }

    public Guest addGuest(Guest guest) {
        guest.setId(UUID.randomUUID());
        storageService.schema.getGuests().add(guest);
        // STORE IT!
        storageService.storageManager.store(storageService.schema.getGuests());
        return guest;
    }

    public boolean checkGuests(List<Guest> guests) {
        guests.stream().findFirst().orElseThrow(IllegalArgumentException::new);
        return true;
    }
}
