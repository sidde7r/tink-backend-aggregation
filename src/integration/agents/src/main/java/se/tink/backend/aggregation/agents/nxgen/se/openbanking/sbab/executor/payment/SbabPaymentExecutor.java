package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.TransferData;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util.TypePair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
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
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SbabPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

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
    private SbabApiClient apiClient;

    public SbabPaymentExecutor(SbabApiClient apiClient) {
        this.apiClient = apiClient;
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

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withTransferData(transferData)
                        .withCreditor(creditorEntity)
                        .withDebtor(debtorEntity)
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, payment.getDebtor().getAccountNumber())
                .toTinkPaymentResponse(
                        getPaymentType(paymentRequest),
                        payment.getDebtor().getAccountNumber(),
                        payment.getCreditor().getAccountNumber());
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
        Payment payment = paymentMultiStepRequest.getPayment();

        apiClient.signPayment(payment.getUniqueId());
        // For sandbox after making a request for signing it's instantly signed
        // On production sign response will have redirect link
        payment.setStatus(PaymentStatus.PAID);

        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
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
