package hr.ogcs.eclipsestore.hotel.domain.booking.api.mcp;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BookingSummary(
        UUID id,
        String roomName,
        List<String> guestNames,
        int guestCount,
        LocalDate from,
        LocalDate to,
        BigDecimal price,
        Booking.PaymentStatus paymentStatus) {

    static BookingSummary from(Booking booking) {
        return new BookingSummary(
                booking.getId(),
                booking.getRoom() != null ? booking.getRoom().getName() : null,
                booking.getGuests().stream().map(Guest::getFullName).toList(),
                booking.getGuests().size(),
                booking.getFrom(),
                booking.getTo(),
                booking.getPrice(),
                booking.getPaymentStatus());
    }
}
