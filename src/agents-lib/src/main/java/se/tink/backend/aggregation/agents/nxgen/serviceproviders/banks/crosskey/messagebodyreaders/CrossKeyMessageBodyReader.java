package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.messagebodyreaders;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class CrossKeyMessageBodyReader extends JacksonJsonProvider {

    private final Package readablePackage;

    public CrossKeyMessageBodyReader(Package readablePackage) {
        this.readablePackage = readablePackage;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.getPackage().getName().startsWith(readablePackage.getName());
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        return super.readFrom(type, genericType, annotations, mediaType, httpHeaders,
                removeUnwantedCharacters(entityStream));
    }

    @VisibleForTesting
    // Usually Cross Key backend responses start with ")]}'," on the first line and the json object on the second.
    // This implementation doesn't care about the exact characters used before the line break.
    public InputStream removeUnwantedCharacters(InputStream entityStream) throws IOException {
        if (entityStream != null) {
            int currentChar;
            while ( (currentChar = entityStream.read()) != -1) {
                if (currentChar == '\n') {
                    return entityStream;
                }
            }
            entityStream.reset();
        }
        return entityStream;
    }

}
