package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device;

import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.OperationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.BaseTlcRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceOperationRequest
        extends BaseTlcRequest<OperationDataEntity, UserIdHeaderEntity> {

    public DeviceOperationRequest(UserIdHeaderEntity header, OperationDataEntity data) {
        super(header, data);
    }
}
