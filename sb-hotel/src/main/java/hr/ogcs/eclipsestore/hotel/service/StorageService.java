package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Schema;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
@Slf4j
public class StorageService {

    protected final Schema schema;
    protected EmbeddedStorageManager storageManager;

    private String path = "hotel";

    private StorageService() {
        this.schema = new Schema();
        this.storageManager = EmbeddedStorage.start(schema, Paths.get(path));
        log.info("StorageManager and Schema successfully initiated.");
    }

    protected void storeRoot() {
        storageManager.storeRoot();
    }

    protected void store(Object obj) {
        storageManager.store(obj);
    }

}
