package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.ScaMetadataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinaliseApprovalRequest {
    private ScaMetadataEntity metaData;

    private FinaliseApprovalRequest(String processContextId) {
        this.metaData = ScaMetadataEntity.create(processContextId);
    }

    public static FinaliseApprovalRequest create(String processContextId) {
        return new FinaliseApprovalRequest(processContextId);
    }
}
