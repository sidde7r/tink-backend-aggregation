package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcAccountKey {
    private String accountId;
    private String agreementId;

    public String getAccountId() {
        return accountId;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public boolean hasSameId(SdcAccountKey entityKey) {
        return this.accountId.equalsIgnoreCase(entityKey.accountId);
    }
}
