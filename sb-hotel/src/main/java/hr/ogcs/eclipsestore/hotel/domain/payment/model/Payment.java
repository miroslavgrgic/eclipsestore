package hr.ogcs.eclipsestore.hotel.domain.payment.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.filter.Filterable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.CONTAINS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.EQUALS;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.GREATER_OR_EQUAL;
import static hr.ogcs.eclipsestore.hotel.domain.filter.FilterOperator.LESS_OR_EQUAL;

// Not a record: EclipseStore can only build a record instance back on load via
// Unsafe-based allocation (jdk.internal.misc.Unsafe), which this app's JVM isn't
// started with access to. A no-arg-constructor class, like every other persisted
// entity here (Guest, Room, Booking), avoids that requirement entirely.
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@ToString
public class Payment {

    private Booking booking;

    @Filterable({EQUALS, CONTAINS})
    private String paymentProviderId;

    @Filterable({EQUALS, GREATER_OR_EQUAL, LESS_OR_EQUAL})
    private Date paymentDate;

}
