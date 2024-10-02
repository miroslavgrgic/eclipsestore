package hr.ogcs.eclipsestore.api;

import hr.ogcs.eclipsestore.model.Guest;
import hr.ogcs.eclipsestore.service.GuestService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/guests")
public class GuestResource {

    @Inject
    private GuestService guestService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Guest> getAllGuests() {
        return guestService.getAllGuests();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Guest createGuest(Guest guest) {
        return guestService.addGuest(guest);
    }

}
