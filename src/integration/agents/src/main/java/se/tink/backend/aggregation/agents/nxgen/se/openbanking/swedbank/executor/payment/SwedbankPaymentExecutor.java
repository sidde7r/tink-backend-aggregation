package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class SwedbankPaymentExecutor implements PaymentExecutor {
    private SwedbankApiClient apiClient;
    private ArrayList<PaymentResponse> createdPaymentsList;

    public SwedbankPaymentExecutor(SwedbankApiClient apiClient) {
        this.apiClient = apiClient;
        createdPaymentsList = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);

        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);

        AmountEntity amount =
                new AmountEntity(
                        paymentRequest.getPayment().getAmount().doubleValue(),
                        paymentRequest.getPayment().getCurrency());

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(creditor, debtor, amount);

        PaymentResponse paymentResponse =
                apiClient
                        .createPayment(
                                createPaymentRequest, getSwedbankePaymentType(paymentRequest))
                        .toTinkPaymentResponse(
                                paymentRequest.getPayment(),
                                getSwedbankePaymentType(paymentRequest));

        createdPaymentsList.add(paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(
                        paymentRequest.getPayment(),
                        apiClient
                                .getPaymentStatus(paymentRequest.getPayment().getUniqueId())
                                .getTransactionStatus());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                PaymentAuthorisationResponse paymentAuthorisationResponse =
                        apiClient.startPaymentAuthorisation(
                                paymentMultiStepRequest.getPayment().getUniqueId());
                PaymentAuthorisationResponse scaAuthorisationStatus =
                        apiClient.getPaymentAuthorisationStatus(
                                paymentAuthorisationResponse.getLinks().getScaStatus().getUrl());
                GetPaymentResponse getPaymentResponse =
                        apiClient.getPayment(paymentMultiStepRequest.getPayment().getUniqueId());
                paymentStatus = PaymentStatus.PAID;
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;

            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
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
        return new PaymentListResponse(createdPaymentsList);
    }

    private SwedbankPaymentType getSwedbankePaymentType(PaymentRequest paymentRequest) {
        Pair<Type, Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiersKey)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No SwedbankPaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    private static final GenericTypeMapper<SwedbankPaymentType, Pair<Type, Type>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper.<SwedbankPaymentType, Pair<Type, Type>>genericBuilder()
                            .put(
                                    SwedbankPaymentType.SeDomesticCreditTransfers,
                                    new Pair<>(Type.SE, Type.SE),
                                    new Pair<>(Type.SE, Type.IBAN),
                                    new Pair<>(Type.SE, Type.SE_BG),
                                    new Pair<>(Type.SE, Type.SE_PG))
                            .put(
                                    SwedbankPaymentType.SeInternationalCreditTransfers,
                                    new Pair<>(Type.IBAN, Type.IBAN))
                            .build();
}
