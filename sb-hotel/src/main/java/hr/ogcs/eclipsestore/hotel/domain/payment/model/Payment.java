package hr.ogcs.eclipsestore.hotel.domain.payment.model;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.filter.Filterable;

import java.util.Date;

import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.CONTAINS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.EQUALS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.GREATER_OR_EQUAL;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.LESS_OR_EQUAL;

public record Payment(
        Booking booking,
        @Filterable({EQUALS, CONTAINS}) String paymentProviderId,
        @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL}) Date paymentDate) {
}
