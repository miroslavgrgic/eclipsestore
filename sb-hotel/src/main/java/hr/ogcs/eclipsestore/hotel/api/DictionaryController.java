package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.model.Schema;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/dicts")
@Slf4j
public class DictionaryController {

    @GetMapping(value = "/schema")
    public ResponseEntity<String> getDictionary() {
        final SerializerFoundation<?> foundation = SerializerFoundation.New()
                .registerEntityTypes(Schema.class);

        Serializer<byte[]> serializer = Serializer.Bytes(foundation);
        var exportedTypeDictionary = serializer.exportTypeDictionay();
        return ResponseEntity.ok(exportedTypeDictionary);
    }

}
