package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.autoauthentication;

import java.security.PrivateKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.DeviceOperationRequest;

@Getter
@RequiredArgsConstructor
class AutoAuthenticationParameters {
    private final String deviceId;

    private final PrivateKey signaturePrivateKey;

    private final DeviceOperationRequest request;
}
