package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class TppMessage {

    private String code;
    private String text;
    private String category;

    @JsonIgnore
    public boolean isServiceBlocked() {
        return ErrorMessages.SERVICE_BLOCKED.equalsIgnoreCase(code);
    }
}
