package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.ConfirmDeviceResponsePayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConfirmDeviceResponse extends BaseResponse {

    private ConfirmDeviceResponsePayload payload;
}
