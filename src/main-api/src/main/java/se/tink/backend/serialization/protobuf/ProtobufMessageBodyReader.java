package se.tink.backend.serialization.protobuf;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.libraries.log.legacy.LogUtils;

@Provider
@Consumes(TinkMediaType.APPLICATION_PROTOBUF)
public class ProtobufMessageBodyReader<T> implements MessageBodyReader<T> {

    private static final LogUtils log = new LogUtils(ProtobufMessageBodyReader.class);

    // Preferably the size of your largest possible string if you are streaming.
    private static final int bufferSize = 8096;
    
    private static final ThreadLocal<LinkedBuffer> localBuffer = ThreadLocal
            .withInitial(() -> LinkedBuffer.allocate(bufferSize));

    public static LinkedBuffer getApplicationBuffer() {
        return localBuffer.get();
    }

    @Override
    public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
        return true;
    }

    @Override
    public T readFrom(Class<T> clazz, Type arg1, Annotation[] arg2, MediaType arg3,
            MultivaluedMap<String, String> arg4, InputStream inputStream) throws IOException, WebApplicationException {

        T t = null;
        LinkedBuffer buffer = getApplicationBuffer();
        Schema<T> schema = RuntimeSchema.getSchema(clazz);

        try {
            t = clazz.newInstance();
            ProtobufIOUtil.mergeFrom(inputStream, t, schema, buffer);

        } catch (InstantiationException e) {
            log.error("ProtobufMessageBodyReader: Could not instantiate class: '" + clazz + "'", e);
        } catch (IllegalAccessException e) {
            log.error("ProtobufMessageBodyReader: IllegalAccessException", e);
        } finally {
            if (buffer != null) {
                buffer.clear();
            }
        }

        return t;
    }
}
