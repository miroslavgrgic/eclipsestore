package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Booking;
import hr.ogcs.eclipsestore.hotel.model.Guest;
import hr.ogcs.eclipsestore.hotel.model.Room;
import hr.ogcs.eclipsestore.hotel.model.Schema;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.TypedSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BookingService {

    private StorageService storageService;
    private GuestService guestService;
    private RoomService roomService;

    @Value("${app.config.consumer-service.endpoint}")
    private String consumerEndpoint;

    public BookingService(StorageService storageService, GuestService guestService, RoomService roomService) {
        this.storageService = storageService;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    public List<Booking> getAllBookings() {
        return storageService.schema.getBookings();
    }

    public Booking createBooking(Booking booking) {
        // check if room exists
//        roomService.findById(booking.getRoom().getId())
//                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + booking.getRoom().getId() + " does not exist"));

        if (booking.getRoom().maxNumberOfGuests() < booking.getGuests().size()) {
            //throw new IllegalArgumentException("Too many guests for this room");
        }

        // check if guest already exists
        List<Guest> potentialNewGuests = new ArrayList<>();
        booking.getGuests().stream().forEach(
                guest -> {
                    // TODO last name is not enough - an dedicated equals could handle it
                    guestService.findByLastname(guest.getLastName())
                        .ifPresent(g -> potentialNewGuests.add(g));
                }
        );

        if (!potentialNewGuests.isEmpty()) {
            booking.getGuests().removeAll(booking.getGuests());
            booking.getGuests().addAll(potentialNewGuests);
        } else {
            booking.getGuests().stream().forEach(guest -> {
                guest.setId(UUID.randomUUID());
                // STORING the new guest in its domain
                guestService.createGuest(guest);
            });
        }

        booking.setId(UUID.randomUUID());
        storageService.schema.getBookings().add(booking);

        // STORE IT!
        storageService.store(storageService.schema.getBookings());
        log.info("Created booking: {}", booking);

        // SEND IT TO CONSUMER SERVICE
        // TODO serialize Booking using EclipseSerializer to send it to the Consumer Service

        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(Schema.class);

        Serializer<byte[]> serializer = Serializer.Bytes(foundation);
        final String typeDictionaryString = serializer.exportTypeDictionay();

        System.out.println("---");
        System.out.println(typeDictionaryString);
        System.out.println("---");

        //final SerializerFoundation<?> foundation = SerializerFoundation.New(typeDictionaryString);
        Serializer<byte[]> typedSerializer = TypedSerializer.Bytes(foundation);
        byte[] data = typedSerializer.serialize(booking);

        var result = RestClient.create().post().uri(consumerEndpoint)
                .body(data)
                .contentType(new MediaType("application", "java"))
                .accept(new MediaType("application", "java"))
                .retrieve()
                .body(String.class);

        Booking response = typedSerializer.deserialize(null);

        log.info(result.toString());

        return booking;
    }

    public void deleteBookingByID(String bookingID) {
        Optional<Booking> booking = storageService.schema.getBookings().stream()
                .filter(item -> item.getId().toString().equals(bookingID))
                .findFirst();

        if (booking.isEmpty()) {
            throw new IllegalArgumentException("Trying to delete entry that does not exist!");
        } else {
            storageService.schema.getBookings().remove(booking.get());
            storageService.storageManager.store(storageService.schema.getBookings());
            log.info("Deleted Booking with ID {}", bookingID);
        }
    }

}
