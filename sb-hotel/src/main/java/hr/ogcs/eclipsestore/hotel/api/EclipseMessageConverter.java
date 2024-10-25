package hr.ogcs.eclipsestore.hotel.api;

import hr.ogcs.eclipsestore.hotel.model.Booking;
import org.eclipse.serializer.Serializer;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EclipseMessageConverter extends AbstractHttpMessageConverter<Booking> {

    public EclipseMessageConverter() {
        super(new MediaType("application", "java", StandardCharsets.UTF_8));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        System.out.println("clazzzzzzzzzz je " + clazz);
        System.out.println("booking.class is assignable from " + Booking.class.isAssignableFrom(clazz));
        return Booking.class.isAssignableFrom(clazz);
    }

    @Override
    protected Booking readInternal(Class<? extends Booking> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        byte[] body = inputMessage.getBody().readAllBytes();
        System.out.println("Received body internal (raw bytes): " + Arrays.toString(body));
        Serializer<byte[]> serializer = Serializer.Bytes();
        Booking test = serializer.deserialize(body);
        System.out.println("Deserialized internal object consumer: " + test);

        return test;
    }

    @Override
    protected void writeInternal(Booking booking, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        System.out.println("write internal....................");
        System.out.println("output message header " + outputMessage.getHeaders().getContentType());
        outputMessage.getHeaders().setContentType(new MediaType("application", "java", StandardCharsets.UTF_8));
        System.out.println("output message header " + outputMessage.getHeaders().getContentType());
        Serializer<byte[]> serializer = Serializer.Bytes();
        byte[] data = serializer.serialize(booking);
        outputMessage.getBody().write(data);
    }
}
