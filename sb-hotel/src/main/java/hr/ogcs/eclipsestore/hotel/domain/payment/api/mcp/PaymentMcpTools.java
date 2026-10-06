package hr.ogcs.eclipsestore.hotel.domain.payment.api.mcp;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.payment.incoming.PaymentPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PaymentMcpTools {

    private static final int DEFAULT_TOP_K = 5;

    private final PaymentPort paymentPort;

    public PaymentMcpTools(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    @Tool(name = "getPayments", description = "Query hotel payments, optionally restricted to a date range. " +
            "Returns, for each payment, the room name, guest names and guest count, stay dates, price and payment status. " +
            "Use this to answer questions about payments, e.g. which bookings are still not paid.")
    public Map<UUID, PaymentSummary> getPayments(
            @ToolParam(description = "Only include payments whose stay ends on or after this date (inclusive). Omit for no lower bound. Format yyyy-mm-dd", required = false)
            LocalDate from,
            @ToolParam(description = "Only include payments whose stay starts on or before this date (inclusive). Omit for no upper bound. Format yyyy-mm-dd", required = false)
            LocalDate to) {

        return paymentPort.getAllPayments().entrySet().stream()
                .filter(entry -> overlaps(entry.getValue().getBooking(), from, to))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> PaymentSummary.from(entry.getValue())));
    }

    private static boolean overlaps(Booking booking, LocalDate from, LocalDate to) {
        boolean endsOnOrAfterFrom = from == null || !booking.getTo().isBefore(from);
        boolean startsOnOrBeforeTo = to == null || !booking.getFrom().isAfter(to);
        return endsOnOrAfterFrom && startsOnOrBeforeTo;
    }
}
