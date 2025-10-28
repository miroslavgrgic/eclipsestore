package hr.ogcs.eclipsestore.hotel.domain.payment.model;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;

import java.util.Date;

public record Payment(Booking booking, String paymentProviderId, Date paymentDate) {
}
