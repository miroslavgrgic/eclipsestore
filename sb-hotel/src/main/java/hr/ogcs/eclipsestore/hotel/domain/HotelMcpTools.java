package hr.ogcs.eclipsestore.hotel.domain;

import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class HotelMcpTools {

    private final StorageService storageService;

    public HotelMcpTools(StorageService storageService) {
        this.storageService = storageService;
    }

    @Tool(name = "getHotel", description = "Query the whole hotel domain including bookings, guests, rooms.")
    public Hotel getCompleteHotel() {
        return storageService.hotel;
    }

}