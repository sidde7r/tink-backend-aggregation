package se.tink.backend.serialization.protobuf;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat;
import io.protostuff.runtime.Delegate;

import java.io.IOException;
import java.util.Date;

public class DateDelegate implements Delegate<Date> {

    @Override
    public WireFormat.FieldType getFieldType() {
        return WireFormat.FieldType.INT64;
    }

    @Override
    public Date readFrom(Input input) throws IOException {
        return new Date(input.readInt64());
    }

    @Override
    public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) throws IOException {
        output.writeInt64(number, input.readInt64(), repeated);
    }

    @Override
    public void writeTo(Output output, int number, Date value, boolean repeated) throws IOException {
        long l = value == null ? 0 : value.getTime();
        output.writeInt64(number, l, repeated);
    }

    @Override
    public Class<?> typeClass() {
        return Date.class;
    }
}
