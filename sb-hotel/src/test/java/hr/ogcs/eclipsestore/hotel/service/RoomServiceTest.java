package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Room;
import hr.ogcs.eclipsestore.hotel.model.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest {

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        StorageService storageServiceMock = Mockito.mock(StorageService.class);

        Schema schemaMock = Mockito.mock(Schema.class);
        //storageServiceMock.schema = schemaMock;
        Mockito.doReturn(schemaMock).when(storageServiceMock.schema);
        Mockito.when(schemaMock.getRooms()).thenReturn(List.of(Room.builder().build()));

        roomService = new RoomService(storageServiceMock);
    }

    @Test
    void getAllRooms() {
        var result = roomService.getAllRooms();

        assertNotNull(result);
    }
}