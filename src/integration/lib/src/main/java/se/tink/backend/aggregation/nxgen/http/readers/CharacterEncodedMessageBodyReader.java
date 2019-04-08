package se.tink.backend.aggregation.nxgen.http.readers;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Message body reader specifically intended to deserialize responses to non UTF-8 encoded String.
 * The standard way for {@link com.sun.jersey.api.client.ClientResponse} to deserialize an HTTP
 * response into {@link String} is to take the character encoding from the response header
 * 'Content-Type' or default to UTF-8. However, if UTF-8 cannot be used this behaviour maybe
 * overridden here.
 *
 * <p><b>NB:</b> The {@link Charset} will be used for every request and response.
 */
public class CharacterEncodedMessageBodyReader extends AbstractMessageReaderWriterProvider<String> {

    private final Charset charset;

    public CharacterEncodedMessageBodyReader(Charset charset) {
        this.charset = charset;
    }

    @Override
    public boolean isReadable(
            Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return type == String.class;
    }

    @Override
    public String readFrom(
            Class<String> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream)
            throws IOException {
        return readFromAsString(entityStream, cloneWithEncoding(mediaType));
    }

    @Override
    public boolean isWriteable(
            Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return type == String.class;
    }

    @Override
    public void writeTo(
            String t,
            Class<?> type,
            Type genericType,
            Annotation annotations[],
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream)
            throws IOException {
        writeToAsString(t, entityStream, cloneWithEncoding(mediaType));
    }

    /** Relies on the implementation of {@link com.sun.jersey.core.util.ReaderWriter#getCharset} */
    private MediaType cloneWithEncoding(MediaType mediaType) {
        return new MediaType(
                mediaType.getType(),
                mediaType.getSubtype(),
                ImmutableMap.of("charset", this.charset.name()));
    }
}
