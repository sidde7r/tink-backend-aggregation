package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentRequest;
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
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class UnicreditPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final UnicreditBaseApiClient apiClient;

    public UnicreditPaymentExecutor(UnicreditBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        PaymentType type =
                UnicreditConstants.PAYMENT_TYPE_MAPPER
                        .translate(paymentRequest.getPayment().getCreditorAndDebtorAccountType())
                        .orElse(PaymentType.UNDEFINED);

        Payment payment = paymentRequest.getPayment();

        AmountEntity amount =
                new AmountEntity(
                        paymentRequest.getPayment().getAmount().getValue().toString(),
                        paymentRequest.getPayment().getAmount().getCurrency());

        AccountEntity debtor = new AccountEntity(payment.getDebtor().getAccountNumber());
        AccountEntity creditor = new AccountEntity(payment.getCreditor().getAccountNumber());
        String unstructuredRemittance = "";
        if (Optional.ofNullable(payment.getReference()).isPresent()) {
            unstructuredRemittance = payment.getReference().getValue();
        }

        CreatePaymentRequest request =
                new CreatePaymentRequest.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withCreditorName(payment.getCreditor().getName())
                        .withUnstructuredRemittance(unstructuredRemittance)
                        .build();

        return apiClient
                .createSepaPayment(request)
                .toTinkPayment(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {

        Payment payment = paymentMultiStepRequest.getPayment();
        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "Create beneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        PaymentType type = paymentRequest.getPayment().getType();

        return apiClient.fetchPayment(paymentId).toTinkPayment(paymentId, type);
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
