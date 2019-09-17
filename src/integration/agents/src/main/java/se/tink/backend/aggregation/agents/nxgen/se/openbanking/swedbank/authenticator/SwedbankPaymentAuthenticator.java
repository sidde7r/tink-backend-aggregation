package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SwedbankPaymentAuthenticator {
    private static final long WAIT_FOR_MINUTES = 2L;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public SwedbankPaymentAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper) {
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    public void openThirdPartyApp(URL authorizeUrl, String state) {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));

        this.supplementalInformationHelper.waitForSupplementalInformation(
                OAuthUtils.formatSupplementalKey(state), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }
}
