package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis;

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.authenticator.DemobankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.entity.ExecutorSignStep;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class DemobankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final DemobankPaymentApiClient apiClient;
    private final DemobankPaymentAuthenticator authenticator;
    private final DemobankStorage storage;

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        final String paymentId = storage.getPaymentId();

        return apiClient.getPayment(paymentId);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient.createPayment(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        final ExecutorSignStep step = ExecutorSignStep.of(paymentMultiStepRequest.getStep());
        switch (step) {
            case AUTHENTICATE:
                return authenticate(paymentMultiStepRequest);

            case CHECK_STATUS:
                return checkStatusAndUpdatePayment(paymentMultiStepRequest.getPayment());
            default:
                throw new IllegalArgumentException(
                        "Unknown step: " + paymentMultiStepRequest.getStep());
        }
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    private PaymentMultiStepResponse authenticate(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentAuthorizationException {

        final String authorizeUrl = storage.getAuthorizeUrl();

        final String authCode = authenticator.authenticate(authorizeUrl);
        final OAuth2Token accessToken = apiClient.exchangeAccessCode(authCode);
        storage.storeAccessToken(accessToken);

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest, ExecutorSignStep.CHECK_STATUS.name(), new ArrayList<>());
    }

    private PaymentMultiStepResponse checkStatusAndUpdatePayment(Payment payment)
            throws PaymentRejectedException {

        final String paymentId = storage.getPaymentId();

        final PaymentStatus paymentStatus = apiClient.getPaymentStatus(paymentId);

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException(
                    "Unexpected payment status: " + paymentStatus.name());
        }

        payment.setStatus(paymentStatus);

        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
    }
}
