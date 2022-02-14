package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentStatusMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentStatusMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;

@Slf4j
@RequiredArgsConstructor
public class CommerzBankPaymentExecutor implements PaymentExecutor {

    private static final int RETRY_ATTEMPTS = 108;
    private static final int SLEEP_TIME_SECOND = 5;

    private final CommerzBankApiClient apiClient;
    private final CommerzBankPaymentAuthenticator paymentAuthenticator;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    private final PaymentMapper<CreatePaymentRequest> paymentMapper;
    private final PaymentStatusMapper paymentStatusMapper;

    public CommerzBankPaymentExecutor(
            CommerzBankApiClient apiClient,
            CommerzBankPaymentAuthenticator paymentAuthenticator,
            SessionStorage sessionStorage,
            Credentials credentials) {
        this(
                apiClient,
                paymentAuthenticator,
                sessionStorage,
                credentials,
                new CommerzBankPaymentMapper(),
                new BasePaymentStatusMapper());
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        HttpResponse httpCreatePaymentResponse =
                apiClient.createPayment(
                        paymentMapper.getPaymentRequest(paymentRequest.getPayment()),
                        credentials.getField(Key.USERNAME));
        saveScaApproachAndLinksToStorage(httpCreatePaymentResponse);

        return httpCreatePaymentResponse
                .getBody(CreatePaymentResponse.class)
                .toTinkPayment(paymentRequest.getPayment());
    }

    private void saveScaApproachAndLinksToStorage(HttpResponse httpCreatePaymentResponse) {
        List<String> scaApproachHeadersList =
                httpCreatePaymentResponse.getHeaders().get("ASPSP-SCA-Approach");
        if (scaApproachHeadersList != null) {
            String scaApproach = scaApproachHeadersList.get(0);
            sessionStorage.put(StorageKeys.SCA_APPROACH, scaApproach);
            log.info("PIS SCA approach - " + scaApproach);
        }
        CreatePaymentResponse createPaymentResponse =
                httpCreatePaymentResponse.getBody(CreatePaymentResponse.class);
        sessionStorage.put(
                StorageKeys.SCA_OAUTH_LINK, createPaymentResponse.getLinks().getScaOAuth());
        sessionStorage.put(
                StorageKeys.SCA_STATUS_LINK, createPaymentResponse.getLinks().getScaStatus());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        if (AuthenticationStepConstants.STEP_INIT.equals(paymentMultiStepRequest.getStep())) {
            paymentAuthenticator.authorizePayment(paymentMultiStepRequest.getPayment());
        }
        return checkStatus(paymentMultiStepRequest);
    }

    private PaymentMultiStepResponse checkStatus(PaymentMultiStepRequest paymentMultiStepRequest) {
        PaymentStatus paymentStatus = pollForFinalStatus(paymentMultiStepRequest);
        paymentMultiStepRequest.getPayment().setStatus(paymentStatus);

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest, AuthenticationStepConstants.STEP_FINALIZE);
    }

    private PaymentStatus pollForFinalStatus(PaymentRequest paymentRequest) {
        Retryer<PaymentStatus> retryer = getPaymentStatusRetryer();
        PaymentStatus paymentStatus;
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME_SECOND,
                RETRY_ATTEMPTS);
        try {
            paymentStatus =
                    retryer.call(
                            () ->
                                    apiClient
                                            .fetchPaymentStatus(paymentRequest.getPayment())
                                            .toTinkPayment(
                                                    paymentRequest.getPayment(),
                                                    paymentStatusMapper)
                                            .getPayment()
                                            .getStatus());

        } catch (ExecutionException | RetryException e) {
            throw new PaymentRejectedException();
        }

        return checkPaymentStatus(paymentStatus);
    }

    protected PaymentStatus checkPaymentStatus(PaymentStatus paymentStatus) {
        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return paymentStatus;
            case PENDING:
            case REJECTED:
                throw new PaymentRejectedException();
            case CANCELLED:
                throw new PaymentCancelledException();
            default:
                log.error("Payment in unexpected status after signing: {}", paymentStatus);
                throw new PaymentAuthorizationException();
        }
    }

    protected Retryer<PaymentStatus> getPaymentStatusRetryer() {
        return RetryerBuilder.<PaymentStatus>newBuilder()
                .retryIfResult(status -> status == PaymentStatus.PENDING)
                .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME_SECOND, TimeUnit.SECONDS))
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
    public PaymentResponse cancel(PaymentRequest paymentRequest) throws PaymentException {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }
}
