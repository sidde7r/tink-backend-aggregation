package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.TppMessagesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private List<TppMessagesEntity> tppMessages;

    public String getErrorCode() {
        return tppMessages.stream()
                .map(TppMessagesEntity::getCode)
                .findFirst()
                .orElse(IngBaseConstants.ErrorMessages.UNKNOWN_ERROR);
    }

    public String getErrorText() {
        return tppMessages.stream()
                .map(TppMessagesEntity::getText)
                .findFirst()
                .orElse(IngBaseConstants.ErrorMessages.UNKNOWN_ERROR);
    }
}
