package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Widget;

public class ScreenUpdateResponseDeserializer extends JsonDeserializer<ScreenUpdateResponse> {

    @Override
    public ScreenUpdateResponse deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {
        List<Widget> widgets = new ArrayList<>();

        JsonNode json = jsonParser.getCodec().readTree(jsonParser);
        json.elements().forEachRemaining(element -> deserializeInto(widgets, jsonParser, element));

        return new ScreenUpdateResponse(widgets);
    }

    public void deserializeInto(List<Widget> widgets, JsonParser jsonParser, JsonNode element) {
        try {
            Widget widget = jsonParser.getCodec().treeToValue(element, Widget.class);
            widgets.add(widget);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
