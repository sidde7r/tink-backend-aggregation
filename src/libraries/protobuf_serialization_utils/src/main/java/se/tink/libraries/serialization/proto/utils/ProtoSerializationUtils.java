package se.tink.libraries.serialization.proto.utils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import se.tink.libraries.log.LogUtils;

/**
 * a utility class to provide a protobuff serialization/deserialization tool
 */
public class ProtoSerializationUtils {
    private static final LogUtils log = new LogUtils(ProtoSerializationUtils.class);

    private static ThreadLocal<LinkedBuffer> linkedBuffer = ThreadLocal
            .withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

    private static LinkedBuffer getApplicationBuffer() {
        return linkedBuffer.get();
    }

    public static <T> List<T> deserializeFromBinary(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        List<T> ts = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            ts = ProtostuffIOUtil.parseListFrom(bais, schema);
        } catch (IOException e) {
            log.error("error while reading the stream", e);
        }
        return ts;
    }

    public static <T> byte[] serializeToBinary(List<T> entity, Class<T> clazz) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            LinkedBuffer applicationBuffer = getApplicationBuffer();
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            ProtostuffIOUtil.writeListTo(baos, entity, schema, applicationBuffer);
            applicationBuffer.clear();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("error while writing the stream", e);
            return null;
        }
    }
}

