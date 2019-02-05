package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

public class BelfiusHolderNameDeserializer extends JsonDeserializer<HolderName> {

    @Override
    public HolderName deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return new HolderName(BelfiusTokenReader.getTextValueAndAdvanceToken(p));
    }

}
