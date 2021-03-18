package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.rpc.CreatePaymentRequest;
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
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public class IcaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private IcaBankenApiClient apiClient;

    public IcaPaymentExecutor(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withDebtorAccount(debtorEntity)
                        .withInstructedAmount(instructedAmountEntity)
                        .withCreditorAccount(creditorEntity)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .build();

        return apiClient
                .createPayment(createPaymentRequest, getPaymentType(paymentRequest))
                .toTinkPaymentResponse();
    }

    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<AccountIdentifierType, AccountIdentifierType> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentTypeMapper
                .translate(accountIdentifiersKey)
                .orElseThrow(
                        () ->
                                new NotImplementedException(
                                        "No PaymentType found for your AccountIdentifiers pair "
                                                + accountIdentifiersKey));
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(
                        paymentRequest.getPayment().getUniqueId(), getPaymentType(paymentRequest))
                .toTinkPaymentResponse();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        throw new NotImplementedException(
                "sign not yet implemented for " + this.getClass().getName());
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
        // Not implemented on banks side done
        List<PaymentResponse> paymentResponses = new ArrayList<>();

        paymentListRequest
                .getPaymentRequestList()
                .forEach(paymentRequest -> paymentResponses.add(fetch(paymentRequest)));
        return new PaymentListResponse(paymentResponses);
    }

    private static final GenericTypeMapper<
                    PaymentType, Pair<AccountIdentifierType, AccountIdentifierType>>
            accountIdentifiersToPaymentTypeMapper =
                    GenericTypeMapper
                            .<PaymentType, Pair<AccountIdentifierType, AccountIdentifierType>>
                                    genericBuilder()
                            .put(
                                    PaymentType.SEPA,
                                    new Pair<>(
                                            AccountIdentifierType.IBAN, AccountIdentifierType.IBAN))
                            .build();
}
