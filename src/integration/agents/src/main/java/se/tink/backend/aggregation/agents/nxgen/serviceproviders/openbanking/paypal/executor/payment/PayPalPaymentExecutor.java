package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment;

import com.google.common.base.Preconditions;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.PersonalPaymentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.PersonalPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.payment.rpc.Payment;

public class PayPalPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final long WAIT_FOR_SECONDS = 30L;

    private final String state;
    private final PayPalApiClient apiClient;
    private static final Random random = new SecureRandom();
    private static final Encoder encoder = Base64.getUrlEncoder();
    private final SupplementalInformationHelper supplementalInformationHelper;

    public PayPalPaymentExecutor(
            PayPalApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.state = generateRandomState();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        PersonalPaymentRequestBody personalPaymentRequestBody =
                PersonalPaymentRequestBody.of(paymentRequest);

        PersonalPaymentResponse paymentTokenResponse =
                apiClient.createPayment(personalPaymentRequestBody);

        return paymentTokenResponse.toTinkResponse();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient.fetchPersonalPaymentDetails(paymentRequest.getPayment()).toTinkResponse();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                Payment payment = paymentMultiStepRequest.getPayment();
                openThirdPartyApp(new URL(payment.getReference().getValue()));
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                ExceptionMessages.UNKNOWN_STEP, paymentMultiStepRequest.getStep()));
        }

        PaymentResponse paymentResponse =
                apiClient
                        .fetchPersonalPaymentDetails(paymentMultiStepRequest.getPayment())
                        .toTinkResponse();

        return new PaymentMultiStepResponse(
                paymentResponse.getPayment(), nextStep, new ArrayList<>());
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        // Convert between payment request list -> payment list responses.
        List<PaymentResponse> responses =
                paymentListRequest.getPaymentRequestList().stream()
                        .map(PaymentRequest::getPayment)
                        .map(PaymentResponse::new)
                        .collect(Collectors.toList());
        return new PaymentListResponse(responses);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = this.getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
        this.collect();
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    public ThirdPartyAppResponse<String> collect() {
        this.supplementalInformationHelper.waitForSupplementalInformation(
                this.formatSupplementalKey(this.state), WAIT_FOR_SECONDS, TimeUnit.SECONDS);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private String formatSupplementalKey(String key) {
        return String.format("tpcb_%s", key);
    }

    private static String generateRandomState() {
        byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }
}
