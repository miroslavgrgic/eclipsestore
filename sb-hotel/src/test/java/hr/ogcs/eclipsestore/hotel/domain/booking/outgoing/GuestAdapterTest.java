package hr.ogcs.eclipsestore.hotel.domain.booking.outgoing;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.incoming.GuestPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class GuestAdapterTest {

    private GuestAdapter guestAdapter;
    private GuestPort guestPortMock;

    @BeforeEach
    void setUp() {
        guestPortMock = Mockito.mock(GuestPort.class);

        guestAdapter = new GuestAdapter(guestPortMock);
    }

    @Test
    void should_upsert_non_existing_guests_with_ids() {
        Guest existingGuest = Guest.builder()
                .id(UUID.fromString("95509042-e462-4c60-a226-2241f8b81907"))
                .firstName("Max")
                .lastName("Mustermann")
                .age(20)
                .build();
        Mockito.when(guestPortMock.findById(UUID.fromString("95509042-e462-4c60-a226-2241f8b81907")))
                .thenReturn(Optional.of(existingGuest));

        Guest newGuestWithId = Guest.builder()
                .id(UUID.randomUUID())
                .firstName("Marina")
                .lastName("Musterfrau")
                .age(21)
                .build();
        Mockito.when(guestPortMock.createGuest(Mockito.any(Guest.class))).thenReturn(newGuestWithId);

        Guest newGuestWithoutId = Guest.builder()
                // no UUID yet
                .firstName("Marina")
                .lastName("Musterfrau")
                .age(21)
                .build();

        List<Guest> guestList = List.of(
                existingGuest,
                newGuestWithoutId
        );

        // when
        guestAdapter.upsert(guestList);

        // then
        Assertions.assertEquals(2, guestList.stream().filter(guest -> guest.getId() != null).count());
    }
}