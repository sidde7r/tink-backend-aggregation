package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthFilterInstantiator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domestic.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.domesticscheduled.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.ExecutorSignStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.ApiClientWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.DomesticPaymentApiClientWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.DomesticScheduledPaymentApiClientWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.helper.UkOpenBankingPaymentHelper;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
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
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@Slf4j
public class UkOpenBankingPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final UkOpenBankingPaymentApiClient apiClient;
    private final Credentials credentials;
    private final UkOpenBankingPaymentHelper ukOpenBankingPaymentHelper;
    private final UkOpenBankingPaymentAuthenticator authenticator;
    private final UkOpenBankingPisAuthFilterInstantiator authFilterInstantiator;

    public UkOpenBankingPaymentExecutor(
            UkOpenBankingPaymentApiClient apiClient,
            Credentials credentials,
            UkOpenBankingPaymentAuthenticator authenticator,
            UkOpenBankingPisAuthFilterInstantiator authFilterInstantiator) {

        this.apiClient = apiClient;
        this.credentials = credentials;
        this.ukOpenBankingPaymentHelper =
                new UkOpenBankingPaymentHelper(this.createApiClientWrapperMap(), Clock.systemUTC());
        this.authenticator = authenticator;
        this.authFilterInstantiator = authFilterInstantiator;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        UkOpenBankingPisUtils.validateRemittanceWithProviderOrThrow(
                credentials.getProviderName(),
                paymentRequest.getPayment().getRemittanceInformation());

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
                return ukOpenBankingPaymentHelper.createConsent(paymentRequest);
            } catch (HttpResponseException e) {
                Uninterruptibles.sleepUninterruptibly(2000 * i, TimeUnit.MILLISECONDS);
            }
        }

        return ukOpenBankingPaymentHelper.createConsent(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return ukOpenBankingPaymentHelper.fetchPaymentIfAlreadyExecutedOrGetConsent(paymentRequest);
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
        RemittanceInformation remittanceInformation =
                paymentMultiStepRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);
        String endToEndIdentification =
                paymentMultiStepRequest.getPayment().getUniqueIdForUKOPenBanking();
        String instructionIdentification = paymentMultiStepRequest.getPayment().getUniqueId();

        PaymentResponse paymentResponse =
                ukOpenBankingPaymentHelper.executePayment(
                        paymentMultiStepRequest, endToEndIdentification, instructionIdentification);

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

    private Map<PaymentType, ApiClientWrapper> createApiClientWrapperMap() {
        final DomesticPaymentApiClientWrapper domesticPaymentApiClientWrapper =
                new DomesticPaymentApiClientWrapper(this.apiClient, new DomesticPaymentConverter());

        return ImmutableMap.of(
                PaymentType.DOMESTIC,
                domesticPaymentApiClientWrapper,
                PaymentType.DOMESTIC_FUTURE,
                new DomesticScheduledPaymentApiClientWrapper(
                        this.apiClient, new DomesticScheduledPaymentConverter()),
                PaymentType.SEPA,
                domesticPaymentApiClientWrapper);
    }
}
