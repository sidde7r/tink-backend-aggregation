package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiScaMethodSelectionStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IccreaScaMethodSelectionStep extends CbiScaMethodSelectionStep {

    public IccreaScaMethodSelectionStep(
            CbiGlobeAuthApiClient authApiClient, URL baseUrlForOperation) {
        super(authApiClient, baseUrlForOperation);
    }

    @Override
    protected ScaMethodEntity selectMethod(List<ScaMethodEntity> scaMethods) {
        // For Iccrea, there is seemingly always a PUSH OTP option available.
        // It was decided long ago that we prefer to pick this, rather than give user choice.
        AuthenticationType authTypePushOtp = AuthenticationType.PUSH_OTP;

        return scaMethods.stream()
                .filter(
                        scaMethod ->
                                authTypePushOtp.name().equals(scaMethod.getAuthenticationType()))
                .findAny()
                .orElseThrow(LoginError.NO_AVAILABLE_SCA_METHODS::exception);
    }
}
