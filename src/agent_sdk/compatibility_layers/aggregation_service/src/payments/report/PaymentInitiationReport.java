package src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.BulkPaymentRegisterBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class PaymentInitiationReport {
    private final Map<Payment, PaymentInitiationState> paymentStates;

    public PaymentInitiationReport(List<Payment> payments) {
        this.paymentStates =
                payments.stream()
                        .collect(
                                Collectors.toMap(
                                        Function.identity(),
                                        payment ->
                                                PaymentInitiationState.builder()
                                                        .paymentReference(
                                                                PaymentReference.builder()
                                                                        .payment(payment)
                                                                        .noBankReference()
                                                                        .build())
                                                        .build()));
    }

    public boolean allPaymentsInFinalState() {
        return this.paymentStates.values().stream().noneMatch(this::isPaymentInProgress);
    }

    public List<PaymentInitiationState> getFinalPaymentStates() {
        return new ArrayList<>(this.paymentStates.values());
    }

    public List<PaymentReference> getInProgressPaymentReferences() {
        return getInProgressPaymentStates()
                .map(PaymentInitiationState::getPaymentReference)
                .collect(Collectors.toList());
    }

    public List<Payment> getInProgressPayments() {
        return getInProgressPaymentStates()
                .map(PaymentInitiationState::getPayment)
                .collect(Collectors.toList());
    }

    private Stream<PaymentInitiationState> getInProgressPaymentStates() {
        return this.paymentStates.values().stream().filter(this::isPaymentInProgress);
    }

    public void updateInProgressPayments(List<PaymentInitiationState> newPaymentStates) {
        if (!existingPaymentStatesAreInProgress(newPaymentStates)) {
            throw new MissingPaymentStateException();
        }

        newPaymentStates.forEach(
                newPaymentState ->
                        this.paymentStates.put(newPaymentState.getPayment(), newPaymentState));
    }

    public void updateInProgressPayments(BulkPaymentRegisterBasketResult registerBasketResult) {
        List<PaymentInitiationState> newPaymentStates =
                registerBasketResult.getPaymentRegisterResults().stream()
                        .map(
                                paymentRegisterResult ->
                                        PaymentInitiationState.builder()
                                                .paymentReference(
                                                        paymentRegisterResult.getReference())
                                                .error(
                                                        paymentRegisterResult
                                                                .getError()
                                                                .orElse(null))
                                                .build())
                        .collect(Collectors.toList());

        updateInProgressPayments(newPaymentStates);
    }

    public void updateInProgressPayments(BulkPaymentSignBasketResult signBasketResult) {
        List<PaymentInitiationState> newPaymentStates =
                signBasketResult.getPaymentSignResults().stream()
                        .map(
                                result ->
                                        PaymentInitiationState.builder()
                                                .paymentReference(result.getReference())
                                                .status(result.getState().getStatus().orElse(null))
                                                .error(result.getState().getError().orElse(null))
                                                .debtor(result.getDebtor().orElse(null))
                                                .build())
                        .collect(Collectors.toList());

        updateInProgressPayments(newPaymentStates);
    }

    private boolean existingPaymentStatesAreInProgress(
            List<PaymentInitiationState> newPaymentStates) {
        List<Payment> existingInProgressPayments = getInProgressPayments();
        List<Payment> newPayments =
                newPaymentStates.stream()
                        .map(PaymentInitiationState::getPayment)
                        .collect(Collectors.toList());

        return existingInProgressPayments.containsAll(newPayments)
                && newPayments.containsAll(existingInProgressPayments);
    }

    private boolean isPaymentInProgress(PaymentInitiationState paymentInitiationState) {
        if (paymentInitiationState.getError().isPresent()) {
            return false;
        }

        PaymentStatus paymentStatus =
                paymentInitiationState.getStatus().orElse(PaymentStatus.CREATED);
        return isPaymentStatusInProgress(paymentStatus);
    }

    private boolean isPaymentStatusInProgress(PaymentStatus paymentStatus) {
        switch (paymentStatus) {
            case CREATED:
                return true;
            case PENDING:
                return true;
            case INITIATED_BUT_NOT_EXECUTED:
                return false;
            case INITIATED_AND_EXECUTED:
                return false;
            default:
                throw new IllegalStateException("Unexpected value: " + paymentStatus);
        }
    }

    public static class MissingPaymentStateException extends RuntimeException {
        public MissingPaymentStateException() {
            super(
                    "Payments to update are not identical to in-progress payments! The agent must have missed to report one or more payments.");
        }
    }
}
