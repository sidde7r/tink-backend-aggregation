package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.entites.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc.GetPaymentStatusResponse;
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
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public abstract class BerlinGroupBasePaymentExecutor
        implements PaymentExecutor, FetchablePaymentExecutor {

    protected List<PaymentResponse> createdPaymentsList;

    protected BerlinGroupBasePaymentExecutor() {
        createdPaymentsList = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withAmount(amount)
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .build();

        BerlinGroupPaymentType paymentType = getPaymentType(paymentRequest);

        PaymentResponse paymentResponse =
                createPayment(createPaymentRequest, paymentType)
                        .toTinkPaymentResponse(paymentRequest.getPayment(), paymentType);

        createdPaymentsList.add(paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        BerlinGroupPaymentType paymentType = getPaymentType(paymentRequest);

        return getPaymentStatus(paymentRequest.getPayment().getUniqueId(), paymentType)
                .toTinkPaymentResponse(paymentRequest.getPayment(), paymentType);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        GetPaymentStatusResponse getPaymentStatusResponse =
                authorizePayment(
                        paymentMultiStepRequest.getPayment().getUniqueId(),
                        getPaymentType(paymentMultiStepRequest));

        PaymentStatus paymentStatus =
                BerlinGroupPaymentStatus.fromString(getPaymentStatusResponse.getTransactionStatus())
                        .getTinkPaymentStatus();

        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, AuthenticationStepConstants.STEP_FINALIZE);
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

    protected abstract CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, BerlinGroupPaymentType paymentType);

    protected abstract GetPaymentStatusResponse getPaymentStatus(
            String paymentId, BerlinGroupPaymentType paymentType);

    protected abstract GetPaymentStatusResponse authorizePayment(
            String paymentId, BerlinGroupPaymentType paymentType);

    protected abstract BerlinGroupPaymentType getPaymentType(PaymentRequest paymentRequest);
}
