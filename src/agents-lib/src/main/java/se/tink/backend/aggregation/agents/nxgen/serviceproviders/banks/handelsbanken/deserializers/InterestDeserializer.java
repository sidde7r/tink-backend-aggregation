package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import se.tink.backend.aggregation.agents.AgentParsingUtils;

public class InterestDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException {
        return AgentParsingUtils.parsePercentageFormInterest(jsonParser.getText());
    }
}
