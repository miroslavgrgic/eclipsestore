package hr.ogcs.eclipsestore.hotel.domain.guest.api.mcp;

import hr.ogcs.eclipsestore.hotel.domain.guest.Address;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.search.GuestSearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GuestMcpTools {

    private static final int DEFAULT_TOP_K = 5;

    private final GuestSearchService guestSearchService;

    public GuestMcpTools(GuestSearchService guestSearchService) {
        this.guestSearchService = guestSearchService;
    }

    @Tool(name = "findSimilarGuests", description = "Find guests most similar to a given, already existing guest, ranked by similarity " +
            "score (0-1, higher is more similar). Similarity is based on age, whether they are a booker, home city/state and last " +
            "name - this is NOT an exact-match search, use getFilteredHotel for that.")
    public List<GuestSearchService.GuestMatch> findSimilarGuests(
            @ToolParam(description = "The UUID of the guest to find similar guests for.") UUID guestId,
            @ToolParam(description = "Maximum number of similar guests to return. Defaults to 5.", required = false) Integer topK) {
        return guestSearchService.findSimilar(guestId, topK == null ? DEFAULT_TOP_K : topK);
    }

    @Tool(name = "searchGuestsByProfile", description = "Find guests most similar to a described profile, ranked by similarity score " +
            "(0-1, higher is more similar). All parameters are optional; omit any you don't care about. Use this to answer " +
            "questions like 'find guests similar to a 30 year old from Munich' without needing an existing guest to compare to.")
    public List<GuestSearchService.GuestMatch> searchGuestsByProfile(
            @ToolParam(description = "Approximate age.", required = false) Integer age,
            @ToolParam(description = "Whether the guest books trips for others.", required = false) Boolean isTheBooker,
            @ToolParam(description = "Last name to match on, e.g. for finding likely family members.", required = false) String lastName,
            @ToolParam(description = "City of residence.", required = false) String city,
            @ToolParam(description = "State/region of residence.", required = false) String state,
            @ToolParam(description = "Maximum number of guests to return. Defaults to 5.", required = false) Integer topK) {

        Guest profile = Guest.builder()
                .age(age == null ? 0 : age)
                .isTheBooker(isTheBooker != null && isTheBooker)
                .lastName(lastName)
                .address(Address.builder().city(city).state(state).build())
                .build();

        return guestSearchService.searchByProfile(profile, topK == null ? DEFAULT_TOP_K : topK);
    }
}
