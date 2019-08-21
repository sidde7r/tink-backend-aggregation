package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BnpParibasFortisPaymentAuthenticator {
    private static final long WAIT_FOR_MINUTES = 2L;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;

    public BnpParibasFortisPaymentAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public void openThirdPartyApp(URL authorizeUrl) {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl));

        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }
}
