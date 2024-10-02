package hr.ogcs.eclipsestore.service;

import hr.ogcs.eclipsestore.model.Schema;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;

import java.nio.file.Paths;

@ApplicationScoped
@Slf4j
public class StorageService {

    protected final Schema schema;
    protected final EmbeddedStorageManager storageManager;

    private final String path = "q-hotel/database";

    public StorageService() {
        this.schema = new Schema();
        this.storageManager = EmbeddedStorage.start(schema, Paths.get(path));
        log.info("StorageManager and Schema successfully initiated in path: {}", path);
    }

}
