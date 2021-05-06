package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.logindevice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.LoginDeviceRequest;

@Getter
@RequiredArgsConstructor
class LoginDeviceParameters {
    private final String deviceId;

    private final String token;

    private final String userId;

    private final LoginDeviceRequest request;
}
