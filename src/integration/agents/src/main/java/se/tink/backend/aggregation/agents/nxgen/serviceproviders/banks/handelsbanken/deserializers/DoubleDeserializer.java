package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import se.tink.libraries.strings.StringUtils;

public class DoubleDeserializer extends StdDeserializer<Double> {
    public DoubleDeserializer() {
        super(Double.class);
    }

    @Override
    public Double deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException {
        String str = jsonParser.getText();
        if (str != null) {
            // replace unicode hyphen with regular hyphen
            str = str.replace("−", "-");
        }
        return StringUtils.parseAmount(str);
    }
}
