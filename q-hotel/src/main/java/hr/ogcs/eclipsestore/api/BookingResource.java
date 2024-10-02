package hr.ogcs.eclipsestore.api;

import hr.ogcs.eclipsestore.model.Booking;
import hr.ogcs.eclipsestore.service.BookingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/bookings")
public class BookingResource {

    @Inject
    private BookingService bookingService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Booking> getBookings() {
        return bookingService.getAllBookings();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Booking createBooking(Booking booking) {
        return bookingService.createBooking(booking);
    }

}
