package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    private String transactionStatus;

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public ConsentStatus getConsentStatus() {
        String consentStatusString = "unknown state";
        try {
            consentStatusString = transactionStatus;
            return ConsentStatus.valueOf(consentStatusString);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    SibsConstants.ErrorMessages.UNKNOWN_TRANSACTION_STATE
                            + "="
                            + consentStatusString,
                    e);
        }
    }
}
