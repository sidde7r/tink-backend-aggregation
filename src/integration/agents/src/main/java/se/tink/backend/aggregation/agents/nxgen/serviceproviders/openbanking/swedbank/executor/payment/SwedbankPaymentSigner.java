package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;

@RequiredArgsConstructor
@Slf4j
public class SwedbankPaymentSigner {
    private final SwedbankOpenBankingPaymentApiClient swedbankApiClient;
    private final SwedbankBankIdSigner swedbankIdSigner;
    private final StrongAuthenticationState strongAuthenticationState;
    private final BankIdSigningController<PaymentMultiStepRequest> signingController;

    public boolean authorize(String paymentId) throws PaymentException {
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

    private boolean isReadyForSigning(String paymentId) throws PaymentException {
        return swedbankApiClient
                .getPaymentStatus(paymentId, SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                .isReadyForSigning();
    }

    private PaymentAuthorisationResponse startAuthorisationProcess(
            String paymentId, boolean isRedirect) throws PaymentException {
        return swedbankApiClient.initiatePaymentAuthorisation(
                paymentId,
                SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                strongAuthenticationState.getState(),
                isRedirect);
    }

    private AuthenticationResponse specifySCAMethod(String selectedAuthenticationMethod)
            throws PaymentException {
        return swedbankApiClient.startPaymentAuthorization(selectedAuthenticationMethod);
    }

    public void sign(PaymentMultiStepRequest request) {
        signingController.sign(request);
    }

    static class MissingExtendedBankIdException extends RuntimeException {}
}
