package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthFilterInstantiator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.ExecutorSignStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.UkOpenBankingPaymentRequestValidator;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
@RequiredArgsConstructor
public class UkOpenBankingPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final UkOpenBankingPaymentApiClient apiClient;
    private final UkOpenBankingPaymentAuthenticator authenticator;
    private final UkOpenBankingPisAuthFilterInstantiator authFilterInstantiator;
    private final UkOpenBankingPaymentRequestValidator paymentRequestValidator;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        paymentRequestValidator.validate(paymentRequest);

        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        return createConsentWithRetry(paymentRequest);
    }

    /**
     * For fixing the Barclays unstable issue; No-sleep retry had been tested but working not well;
     * No-sleep retry will get continuous rejection; Jira had been raised on UKOB directory by other
     * TPPs
     */
    @SuppressWarnings("UnstableApiUsage")
    private PaymentResponse createConsentWithRetry(PaymentRequest paymentRequest) {
        for (int i = 0; i < 3; i++) {
            try {
                return apiClient.createPaymentConsent(paymentRequest);
            } catch (HttpResponseException e) {
                Uninterruptibles.sleepUninterruptibly(2000 * i, TimeUnit.MILLISECONDS);
            }
        }

        return apiClient.createPaymentConsent(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return getPaymentId(paymentRequest)
                .map(apiClient::getPayment)
                .orElseGet(() -> apiClient.getPaymentConsent(getConsentId(paymentRequest)));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        final ExecutorSignStep step = ExecutorSignStep.of(paymentMultiStepRequest.getStep());
        switch (step) {
            case AUTHENTICATE:
                return authenticate(paymentMultiStepRequest);

            case EXECUTE_PAYMENT:
                return executePayment(paymentMultiStepRequest);
            default:
                throw new IllegalArgumentException(
                        "Unknown step: " + paymentMultiStepRequest.getStep());
        }
    }

    private PaymentMultiStepResponse authenticate(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentAuthorizationException {

        final String intentId = paymentMultiStepRequest.getStorage().get("consentId");

        final String authCode = this.authenticator.authenticate(intentId);

        authFilterInstantiator.instantiateAuthFilterWithAccessToken(authCode);

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest,
                ExecutorSignStep.EXECUTE_PAYMENT.name(),
                new ArrayList<>());
    }

    private PaymentMultiStepResponse executePayment(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String endToEndIdentification =
                paymentMultiStepRequest.getPayment().getUniqueIdForUKOPenBanking();
        String instructionIdentification = paymentMultiStepRequest.getPayment().getUniqueId();

        PaymentResponse paymentResponse =
                apiClient.executePayment(
                        paymentMultiStepRequest,
                        getConsentId(paymentMultiStepRequest),
                        endToEndIdentification,
                        instructionIdentification);

        // Should be handled on a higher level than here, but don't want to pollute the
        // payment controller with TransferExecutionException usage. Ticket PAY2-188 will
        // address handling the REJECTED status, then we can remove the logic from here.
        if (PaymentStatus.REJECTED.equals(paymentResponse.getPayment().getStatus())) {
            throw new PaymentRejectedException();
        }

        return new PaymentMultiStepResponse(
                paymentResponse, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
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

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    private static Optional<String> getPaymentId(PaymentRequest paymentRequest) {
        return Optional.ofNullable(
                paymentRequest
                        .getStorage()
                        .get(UkOpenBankingV31PaymentConstants.Storage.PAYMENT_ID));
    }

    public static String getConsentId(PaymentRequest paymentRequest) {
        final Optional<String> maybeConsentId =
                Optional.ofNullable(
                        paymentRequest
                                .getStorage()
                                .get(UkOpenBankingV31PaymentConstants.Storage.CONSENT_ID));

        return maybeConsentId.orElseThrow(
                () -> new IllegalArgumentException(("consentId cannot be null or empty!")));
    }
}
