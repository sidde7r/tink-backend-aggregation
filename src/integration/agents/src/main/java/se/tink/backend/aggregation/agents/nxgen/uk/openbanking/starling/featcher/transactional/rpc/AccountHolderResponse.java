package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHolderResponse {
    private String accountHolderUid;

    private StarlingAccountHolderType accountHolderType;

    public String getAccountHolderUid() {
        return accountHolderUid;
    }

    public StarlingAccountHolderType getAccountHolderType() {
        return accountHolderType;
    }
}
