package hr.ogcs.eclipsestore.hotel.repository;

import hr.ogcs.eclipsestore.hotel.domain.Hotel;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
@Slf4j
public class StorageService {

    public final Hotel hotel;
    public final EmbeddedStorageManager storageManager;

    public StorageService(@Value("${eclipsestore.path}") String path) {
        this.hotel = new Hotel("Hotel Sonne", true);

        this.storageManager = EmbeddedStorage.start(hotel, Paths.get(path));
        log.info("StorageManager and Schema successfully initiated in path: {}", path);
    }

    public void store(Object obj) {
        storageManager.store(obj);
    }

}
