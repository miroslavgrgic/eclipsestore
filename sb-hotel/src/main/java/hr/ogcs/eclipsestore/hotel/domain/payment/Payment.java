package hr.ogcs.eclipsestore.hotel.domain.payment;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;

import java.time.LocalDate;

public record Payment(Booking booking, String paymentProviderId, LocalDate paymentDate) {
}
