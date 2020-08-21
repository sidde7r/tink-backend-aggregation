package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.AliasEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.RequestPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.entities.ToEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities.TransferBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
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
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@AllArgsConstructor
@Slf4j
public class N26PaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final String TOKEN_ID = "tokenId";
    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    private final N26ApiClient apiClient;
    private final AgentConfiguration<N26Configuration> configuration;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationHelper supplementalInformationHelper;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        TransferBodyEntity transferBody = TransferBodyEntity.of(paymentRequest, creditor);

        TokenRequest tokenRequest = createTokenRequest(transferBody);
        String tokenId = fetchToken(tokenRequest);

        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest(tokenId);

        CreatePaymentResponse paymentResponse = apiClient.createPayment(createPaymentRequest);

        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(paymentResponse.getTransfer().getId())
                        .withType(PaymentType.SEPA)
                        .withCurrency(transferBody.getCurrency())
                        .withExactCurrencyAmount(transferBody.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .build());
    }

    private TokenRequest createTokenRequest(TransferBodyEntity transferBody) {
        final N26Configuration n26Configuration = configuration.getProviderSpecificConfiguration();
        final AliasEntity aliasEntity =
                new AliasEntity(
                        n26Configuration.getAliasType(),
                        n26Configuration.getAliasValue(),
                        n26Configuration.getRealmId());
        final ToEntity toEntity = new ToEntity(n26Configuration.getMemberId(), aliasEntity);

        final RequestPayload requestPayload =
                new RequestPayload(
                        strongAuthenticationState.getState(),
                        toEntity,
                        transferBody,
                        configuration.getRedirectUrl());
        return new TokenRequest(requestPayload);
    }

    private String fetchToken(TokenRequest tokenRequest) {
        TokenResponse tokenResponse = apiClient.tokenRequest(tokenRequest);

        Map<String, String> callbackData = new CaseInsensitiveMap<>(getCallbackData(tokenResponse));

        if (!callbackData.containsKey(TOKEN_ID)) {
            throw new IllegalArgumentException("callbackData didn't contain tokenId");
        }

        return callbackData.get(TOKEN_ID);
    }

    private Map<String, String> getCallbackData(TokenResponse tokenResponse) {
        return openThirdPartyAppAndGetCallbackData(
                URL.of(Url.AUTHORIZATION_URL)
                        .parameter(TOKEN_ID, tokenResponse.getTokenRequest().getId()));
    }

    private Map<String, String> openThirdPartyAppAndGetCallbackData(URL authorizationUrl) {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(authorizationUrl));
        return supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES)
                .orElseThrow(() -> new IllegalArgumentException("callbackData wasn't received"));
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest.getPayment().getUniqueId());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        Payment payment = paymentMultiStepRequest.getPayment();
        PaymentStatus paymentStatus = getAndVerifyStatus(payment.getUniqueId());
        payment.setStatus(paymentStatus);

        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, Collections.emptyList());
    }

    private PaymentStatus getAndVerifyStatus(String paymentId) throws PaymentException {
        Retryer<PaymentStatus> paymentStatusRetryer = getPaymentStatusRetryer();

        PaymentStatus paymentStatus;
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME,
                RETRY_ATTEMPTS);
        try {
            paymentStatus =
                    paymentStatusRetryer.call(
                            () -> apiClient.getPayment(paymentId).getPaymentStatus());

        } catch (ExecutionException | RetryException e) {
            log.warn("Payment failed, couldn't fetch payment status");
            throw new PaymentRejectedException("Payment failed, couldn't fetch payment status");
        }

        if (paymentStatus == PaymentStatus.PENDING) {
            throw new PaymentAuthenticationException(
                    "Payment authentication failed.", new PaymentRejectedException());
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException("Unexpected payment status: " + paymentStatus);
        }

        return paymentStatus;
    }

    private Retryer<PaymentStatus> getPaymentStatusRetryer() {
        return RetryerBuilder.<PaymentStatus>newBuilder()
                .retryIfResult(status -> status == PaymentStatus.PENDING)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
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
}
