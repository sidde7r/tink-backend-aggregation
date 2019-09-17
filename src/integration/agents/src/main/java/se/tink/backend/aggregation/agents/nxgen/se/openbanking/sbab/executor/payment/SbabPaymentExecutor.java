package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.PaymentRedirectInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.SignOptionsRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.TransferData;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.enums.SignMethod;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util.TypePair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
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
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SbabPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final long SLEEP_TIME = 10L;
    private static final int RETRY_ATTEMPTS = 60;

    private final SbabApiClient apiClient;
    private final SbabConfiguration clientConfiguration;
    private final SessionStorage sessionStorage;

    private static final GenericTypeMapper<PaymentType, TypePair>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper.<PaymentType, TypePair>genericBuilder()
                            .put(
                                    PaymentType.DOMESTIC,
                                    new TypePair(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.SE),
                                    new TypePair(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.IBAN),
                                    new TypePair(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_BG),
                                    new TypePair(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_PG))
                            .build();

    public SbabPaymentExecutor(
            SbabApiClient apiClient,
            SbabConfiguration clientConfiguration,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.clientConfiguration = clientConfiguration;
        this.sessionStorage = sessionStorage;
    }

    private PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<Type, Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(new TypePair(accountIdentifiersKey))
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No PaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {

        Payment payment = paymentRequest.getPayment();

        CreditorEntity creditorEntity = CreditorEntity.of(paymentRequest);
        DebtorEntity debtorEntity = DebtorEntity.of(paymentRequest);

        TransferData transferData =
                new TransferData.Builder()
                        .withAmount(payment.getAmount().doubleValue())
                        .withCounterPartAccount(payment.getCreditor().getAccountNumber())
                        .withCurrency(payment.getCurrency())
                        .withTransferDate(getExecutionDateOrCurrentDate(payment))
                        .build();

        String state = OAuthUtils.generateNonce();
        URL redirectUrl =
                new URL(clientConfiguration.getRedirectUrl())
                        .queryParam(SbabConstants.QueryKeys.STATE, state);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withTransferData(transferData)
                        .withSignOptionsData(
                                new SignOptionsRequest(
                                        SignMethod.MOBILE.toString(), redirectUrl.toString()))
                        .withCreditor(creditorEntity)
                        .withDebtor(debtorEntity)
                        .build();

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        createPaymentRequest, payment.getDebtor().getAccountNumber());

        PaymentResponse paymentResponse =
                createPaymentResponse.toTinkPaymentResponse(
                        getPaymentType(paymentRequest),
                        payment.getDebtor().getAccountNumber(),
                        payment.getCreditor().getAccountNumber());

        sessionStorage.put(
                paymentResponse.getPayment().getUniqueId(),
                new PaymentRedirectInfoEntity(state, createPaymentResponse.getSignOptions()));

        return paymentResponse;
    }

    private String getExecutionDateOrCurrentDate(Payment payment) {
        LocalDate executionDate =
                payment.getExecutionDate() == null ? LocalDate.now() : payment.getExecutionDate();

        return executionDate.toString();
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        return apiClient
                .getPayment(payment.getUniqueId(), payment.getDebtor().getAccountNumber())
                .toTinkPaymentResponse(
                        getPaymentType(paymentRequest),
                        payment.getDebtor().getAccountNumber(),
                        payment.getCreditor().getAccountNumber());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {

        PaymentRequest payment = new PaymentRequest(paymentMultiStepRequest.getPayment());

        Retryer<PaymentResponse> paymentResponseRetryer =
                RetryerBuilder.<PaymentResponse>newBuilder()
                        .retryIfResult(
                                paymentResponse ->
                                        paymentResponse == null
                                                || !paymentResponse.isStatus(PaymentStatus.SIGNED))
                        .withWaitStrategy(WaitStrategies.fixedWait(SLEEP_TIME, TimeUnit.SECONDS))
                        .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                        .build();

        try {
            PaymentResponse paymentResponse = paymentResponseRetryer.call(() -> fetch(payment));

            return new PaymentMultiStepResponse(
                    paymentResponse.getPayment(),
                    AuthenticationStepConstants.STEP_FINALIZE,
                    new ArrayList<>());
        } catch (RetryException e) {
            throw new IllegalStateException("Max amount of retries exceeded!");
        } catch (ExecutionException e) {
            throw new IllegalStateException("Fetch api error!");
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

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                Optional.ofNullable(paymentListRequest)
                        .map(PaymentListRequest::getPaymentRequestList)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .map(this::fetch)
                        .collect(Collectors.toList()));
    }
}
