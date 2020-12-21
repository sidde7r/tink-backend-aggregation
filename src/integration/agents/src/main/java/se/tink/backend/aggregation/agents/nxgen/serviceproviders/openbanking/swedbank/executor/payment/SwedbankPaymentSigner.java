package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@RequiredArgsConstructor
@Slf4j
public class SwedbankPaymentSigner {
    private final SwedbankOpenBankingPaymentApiClient swedbankApiClient;
    private final SwedbankBankIdSigner swedbankIdSigner;
    private final StrongAuthenticationState strongAuthenticationState;

    public boolean authorize(String paymentId) {
        boolean readyForSigning = isReadyForSigning(paymentId);
        if (readyForSigning) {
            final PaymentAuthorisationResponse paymentAuthorisationResponse =
                    startAuthorisationProcess(paymentId);

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

    private PaymentAuthorisationResponse startAuthorisationProcess(String paymentId) {
        return swedbankApiClient.startPaymentAuthorisation(
                paymentId,
                SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                strongAuthenticationState.getState(),
                false);
    }

    private AuthenticationResponse specifySCAMethod(String selectedAuthenticationMethod) {
        return swedbankApiClient.startPaymentAuthorization(selectedAuthenticationMethod);
    }
}
