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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@Slf4j
@RequiredArgsConstructor
public class CommerzBankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final CommerzBankApiClient apiClient;
    private final CommerzBankPaymentAuthenticator paymentAuthenticator;

    private final SessionStorage sessionStorage;
    private final Credentials credentials;
    private final int retryAttempts;
    private final int sleepTimeSecond;

    public CommerzBankPaymentExecutor(
            CommerzBankApiClient apiClient,
            CommerzBankPaymentAuthenticator paymentAuthenticator,
            SessionStorage sessionStorage,
            Credentials credentials) {
        this.apiClient = apiClient;
        this.paymentAuthenticator = paymentAuthenticator;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.retryAttempts = 108;
        this.sleepTimeSecond = 5;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        HttpResponse response = buildAndSendPaymentRequest(paymentRequest);

        List<String> scaApproachHeadersList = response.getHeaders().get("ASPSP-SCA-Approach");
        if (scaApproachHeadersList != null) {
            String scaApproach = scaApproachHeadersList.get(0);
            sessionStorage.put(StorageKeys.SCA_APPROACH, scaApproach);
            log.info("PIS SCA approach - " + scaApproach);
        }

        CreatePaymentResponse createPaymentResponse = response.getBody(CreatePaymentResponse.class);
        sessionStorage.put(
                StorageKeys.SCA_OAUTH_LINK, createPaymentResponse.getLinks().getScaOAuth());
        sessionStorage.put(
                StorageKeys.SCA_STATUS_LINK, createPaymentResponse.getLinks().getScaStatus());

        return createPaymentResponse.toTinkPayment();
    }

    // Extract from 'parent', reuse
    protected HttpResponse buildAndSendPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        AccountEntity creditor =
                new AccountEntity(
                        payment.getCreditor().getAccountIdentifier(IbanIdentifier.class).getIban());
        AccountEntity debtor =
                new AccountEntity(
                        payment.getDebtor().getAccountIdentifier(IbanIdentifier.class).getIban());

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        creditor,
                        payment.getCreditor().getName(),
                        debtor,
                        new AmountEntity(
                                payment.getCurrency(),
                                payment.getExactCurrencyAmount().getExactValue()),
                        payment.getRemittanceInformation().getValue());

        return apiClient.createPayment(createPaymentRequest, credentials.getField(Key.USERNAME));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        if (AuthenticationStepConstants.STEP_INIT.equals(paymentMultiStepRequest.getStep())) {
            paymentAuthenticator.authorizePayment(paymentMultiStepRequest.getPayment());
        }

        return checkStatus(paymentMultiStepRequest);
    }

    // extract to common class, and behaviour?
    protected PaymentMultiStepResponse checkStatus(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        PaymentStatus paymentStatus = poll(paymentMultiStepRequest.getPayment().getUniqueId());
        paymentMultiStepRequest.getPayment().setStatus(paymentStatus);

        return new PaymentMultiStepResponse(
                paymentMultiStepRequest, AuthenticationStepConstants.STEP_FINALIZE);
    }

    private PaymentStatus poll(String paymentId) {
        Retryer<PaymentStatus> retryer = getPaymentStatusRetryer();
        PaymentStatus paymentStatus;
        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                sleepTimeSecond,
                retryAttempts);
        try {
            paymentStatus =
                    retryer.call(
                            () ->
                                    apiClient
                                            .getPayment(paymentId)
                                            .toTinkPayment(paymentId)
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
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTimeSecond, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }

    // --------------------------- unused crap
    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "fetch not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
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
