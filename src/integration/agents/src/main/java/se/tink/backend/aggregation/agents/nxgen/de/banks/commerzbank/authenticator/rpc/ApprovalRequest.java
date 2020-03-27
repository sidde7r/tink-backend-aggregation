package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.EmptyEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.ScaMetadataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ApprovalRequest {
    private EmptyEntity data;
    private ScaMetadataEntity metaData;

    private ApprovalRequest(String processContextId) {
        this.data = EmptyEntity.create();
        this.metaData = ScaMetadataEntity.create(processContextId);
    }

    public static ApprovalRequest create(String processContextId) {
        return new ApprovalRequest(processContextId);
    }
}
