package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthFilterInstantiator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.credentialsupdater.UkOpenBankingCredentialsUpdater;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
@RequiredArgsConstructor
public class UkOpenBankingPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final UkOpenBankingPaymentApiClient apiClient;
    private final UkOpenBankingPaymentAuthenticator authenticator;
    private final UkOpenBankingPisAuthFilterInstantiator authFilterInstantiator;
    private final UkOpenBankingPaymentRequestValidator paymentRequestValidator;
    private final ProviderSessionCacheController providerSessionCacheController;
    private final UkOpenBankingCredentialsUpdater credentialsUpdater;
    private static final List<Integer> RETRYABLE_STATUSES =
            Arrays.asList(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    HttpStatus.SC_SERVICE_UNAVAILABLE,
                    HttpStatus.SC_BAD_GATEWAY);

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        paymentRequestValidator.validate(paymentRequest);

        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        return apiClient.createPaymentConsent(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        return getPaymentId(paymentRequest)
                .map(id -> apiClient.getPayment(id))
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

        credentialsUpdater.updateCredentialsStatus(CredentialsStatus.UPDATING);

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

        savePaymentId(paymentResponse);

        return new PaymentMultiStepResponse(
                paymentResponse, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
    }

    private void savePaymentId(PaymentResponse paymentResponse) {
        String paymentId =
                paymentResponse.getStorage().get(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY);

        Map<String, String> cache =
                Collections.singletonMap(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY, paymentId);
        providerSessionCacheController.setProviderSessionCacheInfo(cache);
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

    private Optional<String> getPaymentId(PaymentRequest paymentRequest) {
        Storage storage = paymentRequest.getStorage();
        if (!storage.containsKey(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY)) {
            String paymentId = getPaymentIdFromCache();
            storage.put(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY, paymentId);
        }

        return Optional.ofNullable(storage.get(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY));
    }

    private String getPaymentIdFromCache() {
        return providerSessionCacheController
                .getProviderSessionCacheInformation()
                .map(cache -> cache.get(UkOpenBankingPaymentConstants.PAYMENT_ID_KEY))
                .orElse(null);
    }

    private static String getConsentId(PaymentRequest paymentRequest) {
        final Optional<String> maybeConsentId =
                Optional.ofNullable(
                        paymentRequest
                                .getStorage()
                                .get(UkOpenBankingPaymentConstants.CONSENT_ID_KEY));

        return maybeConsentId.orElseThrow(
                () -> new IllegalArgumentException(("consentId cannot be null or empty!")));
    }
}
