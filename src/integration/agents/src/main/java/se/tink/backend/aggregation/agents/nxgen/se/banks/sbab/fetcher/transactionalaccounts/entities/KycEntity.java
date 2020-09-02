package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KycEntity {
    private boolean kycOk;

    public boolean isKycOk() {
        return kycOk;
    }
}
