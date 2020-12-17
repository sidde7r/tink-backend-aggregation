package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.otpverification;

import java.security.PrivateKey;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.asserts.ConfirmChallengeRequest;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
class OtpVerificationParameters {
    private final String sessionId;

    private final String deviceId;

    private final PrivateKey signingHeaderKey;

    private final ConfirmChallengeRequest request;
}
