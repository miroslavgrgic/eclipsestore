package hr.ogcs.eclipsestore.hotel.domain.guest.search;

import hr.ogcs.eclipsestore.hotel.domain.guest.Address;
import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.domain.guest.GuestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GuestSearchServiceTest {

    @Autowired
    GuestService guestService;

    @Autowired
    GuestSearchService guestSearchService;

    @Test
    void should_rank_guests_from_the_same_family_and_city_above_an_unrelated_guest() {
        Guest reference = guestService.createGuest(Guest.builder()
                .firstName("Egon").lastName("Klimt").age(40).isTheBooker(true)
                .address(Address.builder().city("Vienna").state("AT").build())
                .build());
        Guest sameFamily = guestService.createGuest(Guest.builder()
                .firstName("Emilie").lastName("Klimt").age(38)
                .address(Address.builder().city("Vienna").state("AT").build())
                .build());
        Guest unrelated = guestService.createGuest(Guest.builder()
                .firstName("Kenji").lastName("Tanaka").age(25)
                .address(Address.builder().city("Osaka").state("JP").build())
                .build());

        List<GuestSearchService.GuestMatch> matches = guestSearchService.findSimilar(reference.getId(), 200);
        Map<String, Float> scoreById = matches.stream()
                .collect(java.util.stream.Collectors.toMap(m -> m.guest().getId().toString(), GuestSearchService.GuestMatch::score));

        // the reference guest itself must never come back as its own "similar" match
        assertFalse(scoreById.containsKey(reference.getId().toString()));

        assertTrue(scoreById.containsKey(sameFamily.getId().toString()));
        assertTrue(scoreById.containsKey(unrelated.getId().toString()));
        assertTrue(scoreById.get(sameFamily.getId().toString()) > scoreById.get(unrelated.getId().toString()),
                "same-family, same-city guest should score higher than an unrelated guest");
    }

    @Test
    void should_find_guests_similar_to_a_described_profile_without_an_existing_guest() {
        Guest match = guestService.createGuest(Guest.builder()
                .firstName("Frida").lastName("Bergman").age(30)
                .address(Address.builder().city("Stockholm").state("SE").build())
                .build());
        Guest distractor = guestService.createGuest(Guest.builder()
                .firstName("Diego").lastName("Alvarez").age(70)
                .address(Address.builder().city("Lima").state("PE").build())
                .build());

        Guest profile = Guest.builder().age(31).lastName("Bergman")
                .address(Address.builder().city("Stockholm").state("SE").build())
                .build();

        List<GuestSearchService.GuestMatch> matches = guestSearchService.searchByProfile(profile, 200);
        Map<String, Float> scoreById = matches.stream()
                .collect(java.util.stream.Collectors.toMap(m -> m.guest().getId().toString(), GuestSearchService.GuestMatch::score));

        assertTrue(scoreById.get(match.getId().toString()) > scoreById.get(distractor.getId().toString()));
    }

}
