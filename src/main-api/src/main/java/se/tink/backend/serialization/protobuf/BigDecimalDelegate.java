package se.tink.backend.serialization.protobuf;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat;
import io.protostuff.runtime.Delegate;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalDelegate implements Delegate<BigDecimal> {
    @Override
    public WireFormat.FieldType getFieldType() {
        return WireFormat.FieldType.DOUBLE;
    }

    @Override
    public BigDecimal readFrom(Input input) throws IOException {
        return BigDecimal.valueOf(input.readDouble());
    }

    @Override
    public void writeTo(Output output, int i, BigDecimal bigDecimal, boolean b) throws IOException {
        double value = bigDecimal == null ? 0 : bigDecimal.doubleValue();
        output.writeDouble(i, value, b);
    }

    @Override
    public void transfer(Pipe pipe, Input input, Output output, int i, boolean b) throws IOException {
        output.writeDouble(i, input.readDouble(), b);
    }

    @Override
    public Class<?> typeClass() {
        return BigDecimal.class;
    }
}
