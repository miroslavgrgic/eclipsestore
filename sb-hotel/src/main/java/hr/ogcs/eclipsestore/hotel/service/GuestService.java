package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Address;
import hr.ogcs.eclipsestore.hotel.model.Guest;
import hr.ogcs.eclipsestore.hotel.model.Schema;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.TypedSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GuestService {

    @Value("${app.config.consumer-service.endpoint}")
    private String consumerEndpoint;

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

    public boolean sendGuest() {
        // serialize Guest using EclipseSerializer to send it to the Consumer Service
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(Schema.class);
        Serializer<byte[]> typedSerializer = TypedSerializer.Bytes(foundation);

        Guest guest = Guest.builder()
                .firstName("Ime")
                .lastName("Prezime")
                .address((Address.builder()
                        .street("Ulica Petra Preradovića 225")
                        .postalCode(31400)
                        .city("Đakovo")
                        .state("HR")
                        .build()))
                .age(18)
                .build();

        byte[] data = typedSerializer.serialize(guest);

        var result = RestClient.create().post().uri(consumerEndpoint + "/guests")
                .body(data)
                .contentType(new MediaType("application", "java"))
                .accept(new MediaType("application", "java"))
                .retrieve()
                .body(Object.class);

        return true;
    }
}