package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentStatusResponse {

    private String transactionStatus;

    public String getTransactionStatus() {
        return transactionStatus;
    }
}
