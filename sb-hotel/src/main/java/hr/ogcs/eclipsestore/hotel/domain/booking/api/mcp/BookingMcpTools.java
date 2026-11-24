package hr.ogcs.eclipsestore.hotel.domain.booking.api.mcp;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.booking.incoming.BookingPort;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BookingMcpTools {

    private final BookingPort bookingPort;

    public BookingMcpTools(BookingPort bookingPort) {
        this.bookingPort = bookingPort;
    }

    @Tool(name = "getBookings", description = "Query hotel bookings, optionally restricted to a date range. " +
            "A booking is included if its stay overlaps the given range at all. " +
            "Returns, for each booking, the room name, guest names and guest count, stay dates, price and payment status. " +
            "Use this to answer questions about bookings, e.g. which bookings had the most guests in a given month.")
    public List<BookingSummary> getBookings(
            @ToolParam(description = "Only include bookings whose stay ends on or after this date (inclusive). Omit for no lower bound.", required = false)
            LocalDate from,
            @ToolParam(description = "Only include bookings whose stay starts on or before this date (inclusive). Omit for no upper bound.", required = false)
            LocalDate to) {

        return bookingPort.getAllBookings().stream()
                .filter(booking -> overlaps(booking, from, to))
                .map(BookingSummary::from)
                .toList();
    }

    private static boolean overlaps(Booking booking, LocalDate from, LocalDate to) {
        boolean endsOnOrAfterFrom = from == null || !booking.getTo().isBefore(from);
        boolean startsOnOrBeforeTo = to == null || !booking.getFrom().isAfter(to);
        return endsOnOrAfterFrom && startsOnOrBeforeTo;
    }
}
