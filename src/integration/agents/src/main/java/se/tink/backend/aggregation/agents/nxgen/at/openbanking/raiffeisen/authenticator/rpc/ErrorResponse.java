package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity.TppMessagesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private List<TppMessagesEntity> tppMessages;

    public String getTppMessages() {
        return tppMessages.stream()
                .map(TppMessagesEntity::getErrorText)
                .findFirst()
                .orElse(RaiffeisenConstants.ErrorMessages.UNKNOWN_ERROR);
    }
}
