package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BusinessMessageBulk {
    private Object globalIndicator;
    private List<Object> messages;
    private String text;
    private Object pewCode;

    public Object getGlobalIndicator() {
        return globalIndicator;
    }

    public List<Object> getMessages() {
        return messages;
    }

    public String getText() {
        return text;
    }

    public Object getPewCode() {
        return pewCode;
    }
}
