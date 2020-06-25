package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KycEntity {
    private boolean error;
    private boolean kycOk;
    private boolean kycStatusReport;

    public boolean isError() {
        return error;
    }

    public boolean isKycOk() {
        return kycOk;
    }

    public boolean isKycStatusReport() {
        return kycStatusReport;
    }
}
