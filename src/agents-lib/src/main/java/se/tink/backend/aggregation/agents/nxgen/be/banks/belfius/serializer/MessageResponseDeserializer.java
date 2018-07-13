package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;

public class MessageResponseDeserializer extends JsonDeserializer<MessageResponse> {

    @Override
    public MessageResponse deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {

        ArrayNode json = jsonParser.getCodec().readTree(jsonParser);
        String messageContent = json.findValue("messageContent").asText();
        String messageDetail = json.findValue("messageDetail").asText();
        String messageType = json.findValue("messageType").asText();
        String messageTarget = json.findValue("messageTarget").asText();

        return new MessageResponse(messageContent, messageDetail, messageType, messageTarget);
    }
}
