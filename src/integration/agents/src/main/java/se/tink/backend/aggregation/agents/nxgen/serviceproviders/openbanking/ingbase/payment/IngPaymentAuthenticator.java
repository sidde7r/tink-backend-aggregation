package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class IngPaymentAuthenticator {

    private final SupplementalInformationController supplementalInformationController;

    public boolean waitForUserConfirmation(String authorizePaymentUrl) {
        return supplementalInformationController
                .openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(new URL(authorizePaymentUrl)))
                .isPresent();
    }
}
