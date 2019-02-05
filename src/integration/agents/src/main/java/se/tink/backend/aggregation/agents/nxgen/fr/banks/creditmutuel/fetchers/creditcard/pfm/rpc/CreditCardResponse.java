package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities.ViewEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardResponse {
    private String message;
    private String returnCode;
    private ViewEntity view;

    public String getMessage() {
        return message;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public ViewEntity getView() {
        return view;
    }
}
