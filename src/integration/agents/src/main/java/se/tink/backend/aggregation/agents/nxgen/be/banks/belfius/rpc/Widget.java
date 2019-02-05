package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class Widget {

    private String widgetId;

    private JsonNode properties;

    public String getWidgetId() {
        return widgetId;
    }

    public <T> T getProperties(Class<T> c) {
        return properties != null && properties.has(0) ?
                SerializationUtils.deserializeFromTreeNode(properties.get(0), c) : null;
    }

    public String getTextProperty() {
        Text text = getProperties(Text.class);
        return text != null ? text.getText() : null;
    }
}
