package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

@Consumes("text/otml; charset=UTF-8")
class BodyReader extends JacksonJsonProvider {
    @Override
    public boolean isReadable(
            Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }
}
