package se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class NoEscapeOfBackslashMessageBodyWriter extends JacksonJsonProvider {

    private final List<Class> escapableClasses;
    private final ObjectMapper objectMapper;

    public NoEscapeOfBackslashMessageBodyWriter(Class ... escapableClasses) {
        this.escapableClasses = Arrays.asList(escapableClasses);
        objectMapper = new ObjectMapper(new ObjectMapper().getFactory().setCharacterEscapes(new CharacterEscapes() {

            @Override
            public int[] getEscapeCodesForAscii() {
                // add standard set of escaping characters
                int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
                // don't escape backslash (not to corrupt windows path)
                esc['\\'] = CharacterEscapes.ESCAPE_NONE;
                return esc;
            }

            @Override
            public SerializableString getEscapeSequence(int i) {
                // no further escaping (beyond ASCII chars) needed
                return null;
            }
        })).enable(INDENT_OUTPUT);
    }


    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        objectMapper.writeValue(entityStream, value);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return escapableClasses.contains(type);
    }
}
