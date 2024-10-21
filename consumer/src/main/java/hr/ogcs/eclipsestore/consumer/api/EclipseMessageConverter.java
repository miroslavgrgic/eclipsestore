package hr.ogcs.eclipsestore.consumer.api;

import hr.ogcs.eclipsestore.consumer.model.Booking;
import org.eclipse.serializer.Serializer;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.util.List;

public class EclipseMessageConverter implements HttpMessageConverter<Booking> {
    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return List.of(new MediaType("application/java"));
    }

    @Override
    public List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
        return HttpMessageConverter.super.getSupportedMediaTypes(clazz);
    }

    @Override
    public Booking read(Class<? extends Booking> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        Serializer<byte[]> serializer = Serializer.Bytes();
        return serializer.deserialize(inputMessage.getBody().readAllBytes());
    }

    @Override
    public void write(Booking booking, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

    }
}
