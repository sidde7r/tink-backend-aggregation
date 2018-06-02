package se.tink.backend.serialization.protobuf;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat;
import io.protostuff.runtime.Delegate;
import java.io.IOException;
import java.net.URI;

public class URIDelegate implements Delegate<URI> {

    @Override
    public Class<?> typeClass() {
        return URI.class;
    }

    @Override
    public WireFormat.FieldType getFieldType() {
        return WireFormat.FieldType.STRING;
    }

    @Override
    public URI readFrom(Input input) throws IOException {
        String str = input.readString();
        if (str == null) {
            return null;
        }

        return URI.create(str);
    }

    @Override
    public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated) throws IOException {
        output.writeString(number, input.readString(), repeated);
    }

    @Override
    public void writeTo(Output output, int number, URI value, boolean repeated) throws IOException {
        if (value == null) {
            output.writeString(number, null, repeated);
        } else {
            output.writeString(number, value.toString(), repeated);
        }
    }
}
