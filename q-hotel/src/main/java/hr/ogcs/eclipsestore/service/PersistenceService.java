package hr.ogcs.eclipsestore.service;

import hr.ogcs.eclipsestore.model.Schema;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;

import java.nio.file.Paths;

@Singleton
public class PersistenceService {

    @Getter
    private Schema schema;
    @Getter
    private EmbeddedStorageManager storageManager;

    public PersistenceService() {
        this.schema = new Schema();
        this.storageManager = EmbeddedStorage.start(schema, Paths.get("application"));
    }

}
