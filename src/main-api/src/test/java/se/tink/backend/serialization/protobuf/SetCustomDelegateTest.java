package se.tink.backend.serialization.protobuf;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class SetCustomDelegateTest {

    public static class PojoWithLong {
        Long l;

        public PojoWithLong fill() {
            l = new Long(new Date().getTime());
            return this;
        }
    }

    static Delegate<Long> delegate;
    Schema<PojoWithLong> schema;
    PojoWithLong pojo;
    byte[] aSerializedPojo = new byte[] {
            8, 96, 1
    };

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @BeforeClass
    public static void setUpClass() {

        if (RuntimeEnv.ID_STRATEGY instanceof DefaultIdStrategy) {

            delegate = Mockito.mock(Delegate.class);
            when(delegate.typeClass()).thenReturn((Class) Long.class);

            if (!((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(delegate)) {
                delegate = null;
            }
        }
        assertNotNull(delegate);
    }

    @Before
    public void setUp() {
        pojo = new PojoWithLong().fill();
        schema = RuntimeSchema.getSchema(PojoWithLong.class);
    }

    @Test
    public void testCustomDelegateIsCalledForWrites() throws Exception {
        ProtostuffIOUtil.toByteArray(pojo, schema, LinkedBuffer.allocate(512));
        verify(delegate, atLeastOnce()).writeTo(any(Output.class), anyInt(), any(Long.class), anyBoolean());
    }

    @Test
    public void testCustomDelegateIsCalledForReads() throws Exception {
        PojoWithLong incoming = new PojoWithLong();
        ProtostuffIOUtil.mergeFrom(aSerializedPojo, incoming, schema);

        verify(delegate, atLeastOnce()).readFrom(any(Input.class));
    }

    @Test
    public void testCustomDelegateIsCalledThroughMessageBodyWriter() throws Exception {
        ProtobufMessageBodyWriter writer = new ProtobufMessageBodyWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        writer.writeTo(pojo, PojoWithLong.class, null, null, null, null, output);

        verify(delegate, atLeastOnce()).writeTo(any(Output.class), anyInt(), any(Long.class), anyBoolean());
    }

    @Test
    public void testCustomDelegateIsCalledThroughMessageBodyReader() throws Exception {
        ProtobufMessageBodyReader<PojoWithLong> reader = new ProtobufMessageBodyReader<>();
        ByteArrayInputStream input = new ByteArrayInputStream(aSerializedPojo);

        reader.readFrom(PojoWithLong.class, null, null, null, null, input);

        verify(delegate, atLeastOnce()).readFrom(any(Input.class));
    }
}
