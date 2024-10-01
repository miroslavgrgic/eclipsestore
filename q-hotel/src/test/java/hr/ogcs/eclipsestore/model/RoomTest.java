package hr.ogcs.eclipsestore.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    private Room room;

    @Test
    void should_add_additional_bed_size() {
        room = Room.builder()
                .name("Gloria")
                .price(BigDecimal.valueOf(100.00))
                .sqm(55)
                .build();

        assertNotNull(room.getId());
        assertEquals(0, room.getBedSizes().size());

        room.addAdditionalBedSize(90, 200);
        room.addAdditionalBedSize(120, 200);

        assertEquals(2, room.getBedSizes().size());
    }

    @Test
    void should_calculate_max_number_of_guests() {
        room = Room.builder()
                .name("Flora")
                .price(BigDecimal.valueOf(148.50))
                .availableSince(LocalDate.now().minusYears(1))
                .sqm(70)
                .build();

        var result = room.maxNumberOfGuests();
        assertEquals(3, result);
    }
}