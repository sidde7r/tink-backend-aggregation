package se.tink.backend.serialization.protobuf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.WireFormat;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.RuntimeEnv;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class DateDelegateTest {

    static DateDelegate delegate;
    Output output;
    Input input;

    @BeforeClass
    public static void setUpClass() {
        delegate = null;
        if (RuntimeEnv.ID_STRATEGY instanceof DefaultIdStrategy) {
            if (!((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(delegate = new DateDelegate())) {
                delegate = null;
            }
        }

        assertNotNull(delegate);
    }

    @Before
    public void setUp() {
        output = Mockito.mock(Output.class);
        input = Mockito.mock(Input.class);
    }

    @Test
    public void verifyFieldTypeIsInt64() {
        Assert.assertEquals(WireFormat.FieldType.INT64, delegate.getFieldType());
    }

    @Test
    public void verifyTypeClassIsDate() {
        Assert.assertEquals(java.util.Date.class, delegate.typeClass());
    }

    @Test
    public void verifyWritesToVariableInt64() throws Exception {
        Date d = new Date();
        boolean repeat = false;
        delegate.writeTo(output, 1, d, repeat);

        Mockito.verify(output, times(1)).writeInt64(1, d.getTime(), repeat);
        Mockito.verify(output, never()).writeFixed64(anyInt(), anyLong(), anyBoolean());
    }

    @Test
    public void verifyReadsWithVariableInt64() throws Exception {
        delegate.readFrom(input);

        Mockito.verify(input, times(1)).readInt64();
        Mockito.verify(input, never()).readFixed64();
    }

    @Test
    public void verifyReadsReturnsDateWithSameUnderlyingTime() throws Exception {
        Date d = new Date(123456789L);
        Mockito.when(input.readInt64()).thenReturn(d.getTime());

        Date readDate = delegate.readFrom(input);

        assertEquals(d.getTime(), readDate.getTime());
    }
}
