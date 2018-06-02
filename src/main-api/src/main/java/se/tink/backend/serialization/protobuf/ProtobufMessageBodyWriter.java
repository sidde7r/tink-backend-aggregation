package se.tink.backend.serialization.protobuf;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import se.tink.backend.rpc.TinkMediaType;

@SuppressWarnings("rawtypes")
@Provider
@Produces(TinkMediaType.APPLICATION_PROTOBUF)
public class ProtobufMessageBodyWriter implements MessageBodyWriter {

    // Preferably the size of your largest possible string if you are streaming.
    private static final int bufferSize = 8096;

    private static final ThreadLocal<LinkedBuffer> localBuffer = ThreadLocal
            .withInitial(() -> LinkedBuffer.allocate(bufferSize));

    public static LinkedBuffer getApplicationBuffer() {
        return localBuffer.get();
    }

    @Override
    public long getSize(Object entity, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeTo(Object entity, Class type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap headers, OutputStream outputStream) throws IOException, WebApplicationException {

        LinkedBuffer buffer = getApplicationBuffer();

        try {
            Schema schema = RuntimeSchema.getSchema(type);
            ProtobufIOUtil.writeTo(outputStream, entity, schema, buffer);
        } finally {
            buffer.clear();
        }
    }
}
