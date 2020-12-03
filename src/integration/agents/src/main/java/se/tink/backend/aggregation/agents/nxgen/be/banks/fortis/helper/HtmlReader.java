package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

@Consumes({"text/html", "text/html; charset=utf-8"})
public class HtmlReader extends JacksonJsonProvider {

    @Override
    public boolean isReadable(
            Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public Object readFrom(
            Class<Object> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders,
            InputStream entityStream) {
        throw new IllegalStateException(
                String.format("Expected JSON, but MediaType:%s", mediaType.toString()));
    }
}
