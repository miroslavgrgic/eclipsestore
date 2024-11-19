package hr.ogcs.eclipsestore.consumer.api;

import hr.ogcs.eclipsestore.hotel.model.Booking;
import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.serializer.TypedSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EclipseMessageConverter extends AbstractHttpMessageConverter<Booking> {

    public EclipseMessageConverter() {
        super(new MediaType("application", "java", StandardCharsets.UTF_8));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Booking.class.isAssignableFrom(clazz);
    }

    @Override
    protected Booking readInternal(Class<? extends Booking> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        // fetch Dictionary from sb-hotel-service
        RestTemplate restTemplate = new RestTemplate();
        String typDic = restTemplate.getForObject("http://localhost:8080/dicts/schema", String.class);

        // remove leading and trailing "
        var typDicCleaned = typDic.replaceAll("^\"|\"$", "");

        final SerializerFoundation<?> foundation = SerializerFoundation.New(typDicCleaned);
        Serializer<byte[]> typedDeSerializer = TypedSerializer.Bytes(foundation);

        byte[] body = inputMessage.getBody().readAllBytes();
        return typedDeSerializer.deserialize(body);
    }

    @Override
    protected void writeInternal(Booking booking, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        outputMessage.getHeaders().setContentType(new MediaType("application", "java", StandardCharsets.UTF_8));
        Serializer<byte[]> serializer = Serializer.Bytes();
        byte[] data = serializer.serialize(booking);
        outputMessage.getBody().write(data);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
        return super.getSupportedMediaTypes(clazz);
    }
}
