package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Schema;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
@Slf4j
public class StorageService {

    protected final Schema schema;
    protected final EmbeddedStorageManager storageManager;

    public StorageService(@Value("${eclipsestore.path}") String path) {
        this.schema = new Schema();
        this.storageManager = EmbeddedStorage.start(schema, Paths.get(path));
        log.info("StorageManager and Schema successfully initiated in path: {}", path);
    }

    protected void store(Object obj) {
        storageManager.store(obj);
    }

}
