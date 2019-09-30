package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.PhotoTanCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.ScaMetadataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ApprovalRequest {
    private PhotoTanCodeEntity data;
    private ScaMetadataEntity metaData;

    private ApprovalRequest(String photoTanCode, String processContextId) {
        this.data = PhotoTanCodeEntity.create(photoTanCode);
        this.metaData = ScaMetadataEntity.create(processContextId);
    }

    public static ApprovalRequest create(String photoTanCode, String processContextId) {
        return new ApprovalRequest(photoTanCode, processContextId);
    }
}
