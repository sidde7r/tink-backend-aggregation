package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.entities.ErrorMessage;

public class ErrorResponseDeserializer extends StdDeserializer<ErrorResponse> {

    public ErrorResponseDeserializer() {
        super(ErrorResponse.class);
    }

    private String getFieldOrNull(JsonNode node, String fieldName) {
        final JsonNode fieldNode = node.get(fieldName);
        if (Objects.isNull(fieldNode)) {
            return null;
        }
        return fieldNode.asText();
    }

    private ErrorMessage newErrorMessage(JsonNode node) {
        return new ErrorMessage(
                getFieldOrNull(node, "field"),
                getFieldOrNull(node, "message"),
                getFieldOrNull(node, "errorCode"));
    }

    @Override
    public ErrorResponse deserialize(
            com.fasterxml.jackson.core.JsonParser jsonParser,
            com.fasterxml.jackson.databind.DeserializationContext deserializationContext)
            throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        final ObjectNode node = jsonParser.getCodec().readTree(jsonParser);
        final JsonNode message = node.get("message");
        if (Objects.isNull(message)) {
            return new ErrorResponse();
        }
        if (message.isArray()) {
            final List<ErrorMessage> messages =
                    StreamSupport.stream(message.spliterator(), false)
                            .map(this::newErrorMessage)
                            .collect(Collectors.toList());
            return new ErrorResponse(messages);
        }

        return new ErrorResponse(Lists.newArrayList(newErrorMessage(node)));
    }
}
