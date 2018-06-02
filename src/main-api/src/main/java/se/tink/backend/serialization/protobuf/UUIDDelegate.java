package se.tink.backend.serialization.protobuf;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat;
import io.protostuff.runtime.Delegate;
import se.tink.libraries.uuid.UUIDUtils;

import java.io.IOException;
import java.util.UUID;

public class UUIDDelegate implements Delegate<UUID> {

    @Override
    public Class<?> typeClass() {
        return UUID.class;
    }

    @Override
    public WireFormat.FieldType getFieldType() {
        return WireFormat.FieldType.STRING;
    }

    @Override
    public UUID readFrom(Input input) throws IOException {
        String str = input.readString();
        if (str == null) {
            return null;
        }

        return UUIDUtils.fromTinkUUID(str.replace("-", ""));
    }

    @Override
    public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) throws IOException {
        output.writeString(number, input.readString(), repeated);
    }

    @Override
    public void writeTo(Output output, int number, UUID value, boolean repeated) throws IOException {
        output.writeString(number, UUIDUtils.toTinkUUID(value), repeated);
    }
}
