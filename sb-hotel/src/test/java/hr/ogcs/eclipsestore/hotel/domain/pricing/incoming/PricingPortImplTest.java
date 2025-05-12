package hr.ogcs.eclipsestore.hotel.domain.pricing.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.outgoing.RoomAdapter;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingPortImplTest {

    private PricingPort pricingPort;
    private RoomAdapter roomAdapterMock;

    @BeforeEach
    void setUp() {
        roomAdapterMock = Mockito.mock(RoomAdapter.class);
        pricingPort = new PricingPortImpl(roomAdapterMock);
    }

    @Test
    void should_return_xmas_price_surcharge() {
        // given
        Room room = Room.builder()
                .defaultPrice(BigDecimal.valueOf(100))
                .build();

        Mockito.when(roomAdapterMock.findById(Mockito.any(UUID.class))).thenReturn(Optional.ofNullable(room));

        // when
        BigDecimal result = pricingPort.getPriceOfRoom(UUID.randomUUID(),
                LocalDate.of(LocalDate.now().getYear(), 12, 25),
                LocalDate.of(LocalDate.now().getYear(), 12, 29));

        // then
        assertEquals(BigDecimal.valueOf(120.0), result);

    }
}