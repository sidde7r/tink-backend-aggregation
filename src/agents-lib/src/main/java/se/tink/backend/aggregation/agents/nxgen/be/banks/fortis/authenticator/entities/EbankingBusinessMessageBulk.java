package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EbankingBusinessMessageBulk {
    private Object globalIndicator;
    private List<Object> messages;
    private Object text;
    private Object pewCode;

    public Object getGlobalIndicator() {
        return globalIndicator;
    }

    public List<Object> getMessages() {
        return messages;
    }

    public Object getText() {
        return text;
    }

    public Object getPewCode() {
        return pewCode;
    }
}
