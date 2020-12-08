package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState.formatSupplementalKey;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

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
                formatSupplementalKey(state), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }
}
