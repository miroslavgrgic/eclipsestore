package hr.ogcs.eclipsestore.hotel.domain.guest.search;

import hr.ogcs.eclipsestore.hotel.domain.guest.Guest;
import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.store.gigamap.jvector.VectorIndex;
import org.eclipse.store.gigamap.jvector.VectorIndexConfiguration;
import org.eclipse.store.gigamap.jvector.VectorIndices;
import org.eclipse.store.gigamap.jvector.VectorSimilarityFunction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class GuestSearchService {

    private static final String INDEX_NAME = "guest-profile";

    private final StorageService storageService;
    private VectorIndex<Guest> index;

    public GuestSearchService(StorageService storageService) {
        this.storageService = storageService;
    }

    // Safe to run both on a fresh Hotel and on one reloaded from an existing store that
    // already carries this index category: index().register(...) returns null (rather than
    // the existing category) when the category was already registered in a prior run, so the
    // category must be looked up first and only registered if genuinely absent.
    @PostConstruct
    void init() {
        var indices = storageService.hotel.getGuestMap().index();
        VectorIndices<Guest> vectorIndices = indices.get(VectorIndices.Category());
        if (vectorIndices == null) {
            vectorIndices = indices.register(VectorIndices.Category());
        }

        VectorIndexConfiguration configuration = VectorIndexConfiguration
                .builderForMediumDataset(GuestVectorizer.DIMENSION)
                .similarityFunction(VectorSimilarityFunction.COSINE)
                .build();

        this.index = vectorIndices.ensure(INDEX_NAME, configuration, new GuestVectorizer());
        log.info("Guest vector index '{}' ready (dimension={})", INDEX_NAME, GuestVectorizer.DIMENSION);
    }

    public List<GuestMatch> findSimilar(UUID guestId, int topK) {
        Guest guest = storageService.hotel.getGuests().stream()
                .filter(g -> g.getId().equals(guestId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));

        return index.search(guest, topK + 1).stream()
                .filter(entry -> !entry.entity().getId().equals(guestId))
                .limit(topK)
                .map(entry -> new GuestMatch(entry.entity(), entry.score()))
                .toList();
    }

    public List<GuestMatch> searchByProfile(Guest queryProfile, int topK) {
        float[] queryVector = new GuestVectorizer().vectorize(queryProfile);
        return index.search(queryVector, topK).stream()
                .map(entry -> new GuestMatch(entry.entity(), entry.score()))
                .toList();
    }

    public record GuestMatch(Guest guest, float score) {
    }
}
