package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.model.Booking;
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
        return true;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return true;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return List.of(MediaType.valueOf("application/java"));
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

        Serializer<byte[]> serializer = Serializer.Bytes();
        byte[] data = serializer.serialize(booking);
        outputMessage.getHeaders().setContentType(MediaType.valueOf("application/java"));
        outputMessage.getBody().write(data);
    }
}
