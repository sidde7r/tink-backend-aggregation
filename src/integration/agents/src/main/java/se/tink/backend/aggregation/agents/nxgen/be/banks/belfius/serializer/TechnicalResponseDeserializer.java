package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.TechnicalResponse;

public class TechnicalResponseDeserializer extends JsonDeserializer<TechnicalResponse> {

    @Override
    public TechnicalResponse deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {

        ArrayNode json = jsonParser.getCodec().readTree(jsonParser);
        String type = findValue(json, "type").asText();
        Long remainingTimeBeforeSessionTimeout =
                findValue(json, "remainingTimeBeforeSessionTimeout").asLong();

        return new TechnicalResponse(type, remainingTimeBeforeSessionTimeout);
    }

    private JsonNode findValue(JsonNode json, String key) {
        return Optional.ofNullable(json.findValue(key)).orElse(MissingNode.getInstance());
    }
}
