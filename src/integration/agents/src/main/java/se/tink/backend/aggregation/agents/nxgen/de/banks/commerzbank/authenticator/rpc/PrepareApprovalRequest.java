package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.RequestedScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.ScaMetadataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareApprovalRequest {
    private RequestedScaMethodEntity data;
    private ScaMetadataEntity metaData;

    private PrepareApprovalRequest(String scaMethod, String processContextId) {
        this.data = RequestedScaMethodEntity.create(scaMethod);
        this.metaData = ScaMetadataEntity.create(processContextId);
    }

    public static PrepareApprovalRequest create(String scaMethod, String processContextId) {
        return new PrepareApprovalRequest(scaMethod, processContextId);
    }
}
