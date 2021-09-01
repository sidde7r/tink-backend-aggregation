package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class NordeaSingleScaPaymentAuthenticator {

    private final SupplementalInformationController supplementalInformationController;

    public void authenticate(String authorizePaymentUrl)
            throws PaymentAuthorizationException, PaymentCancelledException {
        Optional<Map<String, String>> resultOptional =
                supplementalInformationController.openThirdPartyAppSync(
                        ThirdPartyAppAuthenticationPayload.of(new URL(authorizePaymentUrl)));
        if (resultOptional.isPresent()) {
            handleErrorsIfPresent(resultOptional.get());
        } else {
            throw new PaymentCancelledException("Payment abandoned by user");
        }
    }

    private void handleErrorsIfPresent(Map<String, String> result)
            throws PaymentAuthorizationException {
        if (NordeaBaseConstants.ErrorCodes.CANCELLED.equalsIgnoreCase(result.get("error"))) {
            throw new PaymentAuthorizationException("ThirdPartyApp signing cancelled by the user.");
        }
    }
}
