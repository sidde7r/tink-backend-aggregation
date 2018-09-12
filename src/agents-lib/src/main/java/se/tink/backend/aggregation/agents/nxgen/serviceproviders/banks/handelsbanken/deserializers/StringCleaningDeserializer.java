package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.utils.SHBUtils;

public class StringCleaningDeserializer extends StdDeserializer<String> {

    public StringCleaningDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String input = p.getText();
        if (input == null) {
            return "";
        }
        return SHBUtils.unescapeAndCleanTransactionDescription(input);
    }
}
