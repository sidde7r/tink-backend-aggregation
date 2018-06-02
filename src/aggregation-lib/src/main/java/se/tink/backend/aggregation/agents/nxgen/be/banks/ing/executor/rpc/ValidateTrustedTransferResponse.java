package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.entities.ValidateExternalTransferResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateTrustedTransferResponse {
    private ValidateExternalTransferResponseEntity mobileResponse;

    public ValidateExternalTransferResponseEntity getMobileResponse() {
        return mobileResponse;
    }
}
