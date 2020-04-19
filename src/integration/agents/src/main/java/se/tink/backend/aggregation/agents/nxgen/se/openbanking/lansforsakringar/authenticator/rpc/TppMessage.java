package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppMessage {

    private String code;
    private String text;
    private String category;

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public String getCategory() {
        return category;
    }

    public boolean isServiceBlocked() {
        return this.getCode().equalsIgnoreCase(ErrorMessages.SERVICE_BLOCKED);
    }
}
