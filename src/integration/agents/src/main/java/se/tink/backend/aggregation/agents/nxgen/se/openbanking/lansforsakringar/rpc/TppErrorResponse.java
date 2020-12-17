package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.TppMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppErrorResponse {

    private List<TppMessage> tppMessages;

    public List<TppMessage> getTppMessages() {
        return tppMessages;
    }

    public boolean isAnyServiceBlocked() {
        return tppMessages.stream().anyMatch(TppMessage::isServiceBlocked);
    }
}
