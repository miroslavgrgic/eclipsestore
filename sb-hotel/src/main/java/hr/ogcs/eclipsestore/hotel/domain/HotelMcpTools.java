package hr.ogcs.eclipsestore.hotel.domain;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.filter.EntityFilter;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.payment.model.Payment;
import hr.ogcs.eclipsestore.hotel.domain.room.Room;
import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class HotelMcpTools {

    private final StorageService storageService;

    public HotelMcpTools(StorageService storageService) {
        this.storageService = storageService;
    }

    @Tool(name = "getHotel", description = "Query the whole hotel domain including bookings, guests, rooms, payments.")
    public Hotel getCompleteHotel() {
        return storageService.hotel;
    }

    @Tool(name = "getFilteredHotel", description = "Query the hotel's data restricted to the bookings, guests, rooms and payments matching the " +
            "given criteria. Each criteria map's keys are the filterable field names of the corresponding entity. Suffix a key with " +
            "'_gte' or '_lte' to filter numeric/date fields by range, '_contains' for a case-insensitive substring match on text fields, " +
            "or '_in' to match any value from a list. Omit a criteria map (or pass null/empty) to include all entities of that type.")
    public Hotel getFilteredHotel(
            @ToolParam(description = "Booking filter criteria. " +
                    "Filterable fields: id, from, to, price, paymentStatus (OPEN, PAID, CANCELLED)." +
                    "A booking has minimum one guest older than 18 years."
                    , required = false)
            Map<String, Object> bookingCriteria,
            @ToolParam(description = "Guest (customer) filter criteria. Filterable fields: id, firstName, lastName, age.", required = false)
            Map<String, Object> guestCriteria,
            @ToolParam(description = "Room filter criteria. " +
                    "Filterable fields: id, name, defaultPrice, sqm, canBeUsedWithHandicaps, state (FREE, BLOCKED), availableSince.", required = false)
            Map<String, Object> roomCriteria,
            @ToolParam(description = "Payment filter criteria. Filterable fields: paymentProviderId, paymentDate.", required = false)
            Map<String, Object> paymentCriteria) {

        log.info("getFilteredHotel payload received from MCP client: [bookingCriteria={}, guestCriteria={}, " +
                        "roomCriteria={}, paymentCriteria={}]",
                bookingCriteria, guestCriteria, roomCriteria, paymentCriteria);

        long startTime = System.currentTimeMillis();

        Hotel hotel = storageService.hotel;

        List<Guest> guests = EntityFilter.filter(hotel.getGuests(), guestCriteria);
        List<Room> rooms = EntityFilter.filter(hotel.getRooms(), roomCriteria);
        List<Booking> bookings = EntityFilter.filter(hotel.getBookings(), bookingCriteria);
        Map<UUID, Payment> payments = EntityFilter.filterValues(hotel.getPayments(), paymentCriteria);

        // TODO when criteria is null than entities must be null!
// prompt: any guest from Spain? result will include all guests!!! can be cool, but if huge number: critical payload size!
        Hotel filteredHotel = new Hotel(hotel.getName(), hotel.acceptsCreditCards(), rooms, guests, bookings, payments);

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("getFilteredHotel resolved in {} ms, payload sent to MCP client: [rooms={}, guests={}, bookings={}, payments={}]",
                durationMs, rooms, guests, bookings, payments.values());

        return filteredHotel;
    }

}