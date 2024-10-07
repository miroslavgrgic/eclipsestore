package hr.ogcs.eclipsestore.service;

import hr.ogcs.eclipsestore.model.Schema;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;

import java.nio.file.Paths;

@Slf4j
public class StorageService {

    public final static Schema schema = new Schema();
    public static EmbeddedStorageManager storageManager;

    private final static String path = "q-hotel/database";

    static  {
        storageManager = EmbeddedStorage.start(schema, Paths.get(path));
        log.info("StorageManager and Schema successfully initiated in path: {}", path);
    }

}
