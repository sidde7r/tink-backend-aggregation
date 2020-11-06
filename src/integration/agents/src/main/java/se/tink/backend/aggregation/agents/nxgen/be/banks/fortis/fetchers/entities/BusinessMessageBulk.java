package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.Errorable;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.PewCodeVerifier;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BusinessMessageBulk implements Errorable {
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

    @Override
    public void checkError() {
        PewCodeVerifier.checkPewCode(pewCode);
    }
}
