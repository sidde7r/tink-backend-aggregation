package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.executor.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.LinkTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.shared.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.order.PaymentRequestBody;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
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

public class PayPalOrderTransactionExecutor implements PaymentExecutor {

    private static final long WAIT_FOR_SECONDS = 30L;

    private final String state;
    private final PayPalApiClient apiClient;
    private static final Random random = new SecureRandom();
    private static final Encoder encoder = Base64.getUrlEncoder();
    private final SupplementalInformationHelper supplementalInformationHelper;

    public PayPalOrderTransactionExecutor(
            PayPalApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.state = generateRandomState();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        PaymentRequestBody requestBody = PaymentRequestBody.of(paymentRequest);
        return apiClient.createPayment(requestBody).toTinkPayment();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .fetchOrderTransactionDetails(paymentRequest.getPayment().getUniqueId())
                .toTinkPayment();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String nextStep;
        String stringLinks = paymentMultiStepRequest.getPayment().getReference().getValue();

        List<LinkEntity> links =
                PayPalUtil.fromJSON(new TypeReference<List<LinkEntity>>() {}, stringLinks);
        LinkEntity link =
                PayPalUtil.findByRelation(links, LinkTypes.APPROVE)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                String.format(
                                                        "No link with type: %s",
                                                        LinkTypes.APPROVE)));
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                URL toOpen = new URL(link.getReference());
                openThirdPartyApp(toOpen);
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        PaymentResponse details =
                apiClient
                        .fetchOrderTransactionDetails(
                                paymentMultiStepRequest.getPayment().getUniqueId())
                        .toTinkPayment();
        return new PaymentMultiStepResponse(details.getPayment(), nextStep, new ArrayList<>());
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
        List<PaymentResponse> responses =
                Optional.ofNullable(paymentListRequest)
                        .map(PaymentListRequest::getPaymentRequestList)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .map(this::fetch)
                        .collect(Collectors.toList());

        return new PaymentListResponse(responses);
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = this.getAppPayload(authorizeUrl);
        Preconditions.checkNotNull(payload);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
        ThirdPartyAppResponse<String> response =
                ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
        this.collect(response.getReference());
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        Android androidPayload = new Android();
        androidPayload.setIntent(authorizeUrl.get());
        payload.setAndroid(androidPayload);
        Ios iOsPayload = new Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        payload.setIos(iOsPayload);
        return payload;
    }

    public ThirdPartyAppResponse<String> collect(String reference) {
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
