package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.TppMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppErrorResponse {

    @Getter private List<TppMessage> tppMessages;

    @JsonIgnore
    public boolean isAnyServiceBlocked() {
        return tppMessages.stream().anyMatch(TppMessage::isServiceBlocked);
    }
}
