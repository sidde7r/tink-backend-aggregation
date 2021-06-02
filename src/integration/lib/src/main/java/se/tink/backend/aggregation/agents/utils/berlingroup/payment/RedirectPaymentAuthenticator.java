package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class RedirectPaymentAuthenticator implements PaymentAuthenticator {
    private final SupplementalInformationController supplementalInformationController;
    private final StrongAuthenticationState strongAuthenticationState;
    private static final long WAIT_FOR_MINUTES = 9L;

    @Override
    public void authenticatePayment(LinksEntity scaLinks) {

        Href redirectUrl = scaLinks.getScaRedirect();
        supplementalInformationController.openThirdPartyAppAsync(
                (ThirdPartyAppAuthenticationPayload.of(new URL(redirectUrl.getHref()))));
        supplementalInformationController.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }
}
