package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;

public class SessionOpenedResponseDeserializer extends JsonDeserializer<SessionOpenedResponse> {

    @Override
    public SessionOpenedResponse deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {

        ArrayNode json = jsonParser.getCodec().readTree(jsonParser);
        String sessionId = json.findValue("sessionId").asText();
        int heartbeatInterval = json.findValue("heartbeatInterval").asInt();
        String machineIdentifier = json.findValue("machineIdentifier").asText();

        return new SessionOpenedResponse(sessionId, machineIdentifier, heartbeatInterval);
    }
}
