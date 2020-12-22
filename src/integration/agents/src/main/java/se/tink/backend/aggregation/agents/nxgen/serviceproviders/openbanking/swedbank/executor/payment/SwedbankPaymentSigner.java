package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.SwedbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class SwedbankPaymentSigner {
    private final SwedbankOpenBankingPaymentApiClient swedbankApiClient;
    private final SwedbankBankIdSigner swedbankIdSigner;
    private final StrongAuthenticationState strongAuthenticationState;
    private final BankIdSigningController<PaymentMultiStepRequest> signingController;
    private final SwedbankPaymentAuthenticator paymentAuthenticator;

    public boolean authorize(String paymentId) {
        boolean readyForSigning = isReadyForSigning(paymentId);
        if (readyForSigning) {
            final PaymentAuthorisationResponse paymentAuthorisationResponse =
                    startAuthorisationProcess(paymentId, false);

            final AuthenticationResponse authenticationResponse =
                    specifySCAMethod(paymentAuthorisationResponse.getSelectAuthenticationMethod());

            swedbankIdSigner.setAuthenticationResponse(authenticationResponse);

            return true;
        }
        log.info("Can't start payment authorisation, payment is not ready to sign.");
        return false;
    }

    private boolean isReadyForSigning(String paymentId) {
        return swedbankApiClient
                .getPaymentStatus(paymentId, SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                .isReadyForSigning();
    }

    private PaymentAuthorisationResponse startAuthorisationProcess(
            String paymentId, boolean isRedirect) {
        return swedbankApiClient.startPaymentAuthorisation(
                paymentId,
                SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                strongAuthenticationState.getState(),
                isRedirect);
    }

    private AuthenticationResponse specifySCAMethod(String selectedAuthenticationMethod) {
        return swedbankApiClient.startPaymentAuthorization(selectedAuthenticationMethod);
    }

    public void sign(PaymentMultiStepRequest request) {
        this.signingController.sign(request);

        // left as it was until I'm a bit more brave to dig into changing lib code and the way we
        // handle BankIdStatus. It's ugly and has double dependency on SigningController and Signer.
        if (swedbankIdSigner.isMissingExtendedBankId()) {
            throw new MissingExtendedBankIdException();
        }
    }

    public void signWithRedirect(String paymentId) {
        final String state = strongAuthenticationState.getState();
        final PaymentAuthorisationResponse paymentAuthResponse =
                startAuthorisationProcess(paymentId, true);
        URL redirectUrl = paymentAuthResponse.getScaRedirectUrl();
        paymentAuthenticator.openThirdPartyApp(redirectUrl, state);
    }

    static class MissingExtendedBankIdException extends RuntimeException {}
}
