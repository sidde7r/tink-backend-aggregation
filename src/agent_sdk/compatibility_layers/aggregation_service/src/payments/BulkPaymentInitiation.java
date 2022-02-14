package src.agent_sdk.compatibility_layers.aggregation_service.src.payments;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.models.payments.BulkPaymentSigningBasketImpl;
import se.tink.agent.runtime.payments.RuntimePaymentsApi;
import se.tink.agent.runtime.payments.bulk.RuntimeBulkPaymentInitiator;
import se.tink.agent.runtime.payments.global_signing_basket.RuntimeUnsignedPaymentsDeleter;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.BulkPaymentRegisterBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.unsigned_payment.UnsignedPayment;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.transfer.rpc.Transfer;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.beneficiary.BeneficiaryRegistration;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationState;
import src.agent_sdk.compatibility_layers.aggregation_service.src.steppable_execution.AggregationServiceSteppableExecutor;

public class BulkPaymentInitiation {
    private static final Duration MAX_SIGN_STATUS_POLL_TIME = Duration.ofMinutes(5);

    private final Sleeper sleeper;
    private final AggregationServiceSteppableExecutor steppableExecutor;
    private final RuntimePaymentsApi runtimePaymentsApi;
    private final BeneficiaryRegistration beneficiaryRegistration;
    private final Duration maxSignStatusPollTime;

    public BulkPaymentInitiation(
            SupplementalInformationController supplementalInformationController,
            AgentInstance agentInstance,
            Duration maxSignStatusPollTime) {
        this.sleeper = agentInstance.getUtilities().getSleeper();
        this.steppableExecutor =
                new AggregationServiceSteppableExecutor(
                        supplementalInformationController,
                        agentInstance.getOperation().getAgentStorage());

        this.runtimePaymentsApi = new RuntimePaymentsApi(agentInstance);
        this.beneficiaryRegistration =
                new BeneficiaryRegistration(steppableExecutor, runtimePaymentsApi);
        this.maxSignStatusPollTime = maxSignStatusPollTime;
    }

    public BulkPaymentInitiation(
            SupplementalInformationController supplementalInformationController,
            AgentInstance agentInstance) {
        this(supplementalInformationController, agentInstance, MAX_SIGN_STATUS_POLL_TIME);
    }

    public PaymentInitiationReport initiateBulkPaymentsWithRpcTransfers(List<Transfer> transfers) {
        List<Payment> payments = PaymentsModelConverter.mapTransfers(transfers);
        return initiateBulkPayments(payments);
    }

    public PaymentInitiationReport initiateBulkPaymentsWithRpcPayments(
            List<se.tink.libraries.payment.rpc.Payment> rpcPayments) {
        List<Payment> payments = PaymentsModelConverter.mapPayments(rpcPayments);
        return initiateBulkPayments(payments);
    }

    public PaymentInitiationReport initiateBulkPayments(List<Payment> payments) {
        RuntimeBulkPaymentInitiator bulkPaymentInitiator =
                this.runtimePaymentsApi
                        .getBulkPaymentInitiator()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Agent does not implement bulk payment initiation."));

        PaymentInitiationReport paymentInitiationReport = new PaymentInitiationReport(payments);

        boolean allUnsignedPaymentsWereDeleted = deleteUnsignedPayments();
        if (!allUnsignedPaymentsWereDeleted) {
            throw new FailedToDeleteUnsignedPaymentsException();
        }

        List<PaymentInitiationState> beneficiaryRegistrationStates =
                beneficiaryRegistration.registerBeneficiariesForPayments(
                        paymentInitiationReport.getInProgressPayments());

        paymentInitiationReport.updateInProgressPayments(beneficiaryRegistrationStates);
        if (paymentInitiationReport.allPaymentsInFinalState()) {
            return paymentInitiationReport;
        }

        return registerAndSignPayments(bulkPaymentInitiator, paymentInitiationReport);
    }

    private PaymentInitiationReport registerAndSignPayments(
            RuntimeBulkPaymentInitiator bulkPaymentInitiator,
            PaymentInitiationReport paymentInitiationReport) {

        BulkPaymentRegisterBasketResult registerBasketResult =
                bulkPaymentInitiator.registerPayments(
                        paymentInitiationReport.getInProgressPayments());

        paymentInitiationReport.updateInProgressPayments(registerBasketResult);
        if (paymentInitiationReport.allPaymentsInFinalState()) {
            return paymentInitiationReport;
        }

        BulkPaymentSigningBasket signingBasket =
                new BulkPaymentSigningBasketImpl(
                        registerBasketResult.getBankBasketReference().orElse(null),
                        paymentInitiationReport.getInProgressPaymentReferences());

        BulkPaymentSignBasketResult signBasketResult =
                steppableExecutor.execute(bulkPaymentInitiator.getSignFlow(), signingBasket);

        paymentInitiationReport.updateInProgressPayments(signBasketResult);
        if (paymentInitiationReport.allPaymentsInFinalState()) {
            return paymentInitiationReport;
        }

        Instant startTime = Instant.now();
        while (true) {
            BulkPaymentSigningBasket signedBasket =
                    new BulkPaymentSigningBasketImpl(
                            registerBasketResult.getBankBasketReference().orElse(null),
                            paymentInitiationReport.getInProgressPaymentReferences());

            BulkPaymentSignBasketResult signStatus =
                    bulkPaymentInitiator.getSignStatus(signedBasket);

            paymentInitiationReport.updateInProgressPayments(signStatus);

            if (shouldStopPolling(startTime, paymentInitiationReport)) {
                break;
            }
            this.sleeper.sleep(Duration.ofSeconds(1));
        }

        return paymentInitiationReport;
    }

    /**
     * This method evaluates if it's possible to continue polling for a signing status.
     *
     * @return `true` if we have exceeded execution time or all payments are in a final state,
     *     otherwise `false`.
     */
    private boolean shouldStopPolling(
            Instant startTime, PaymentInitiationReport paymentInitiationReport) {
        Instant currentTime = Instant.now();
        Duration timeSpent = Duration.between(startTime, currentTime);
        return timeSpent.compareTo(this.maxSignStatusPollTime) >= 0
                || paymentInitiationReport.allPaymentsInFinalState();
    }

    /**
     * List and delete all unsigned payments (if any).
     *
     * @return boolean, `true` if all payments were successfully deleted or `false` if one or more
     *     failed to delete.
     */
    private boolean deleteUnsignedPayments() {
        Optional<RuntimeUnsignedPaymentsDeleter> maybeUnsignedPaymentsDeleter =
                this.runtimePaymentsApi.getUnsignedPaymentsDeleter();
        if (!maybeUnsignedPaymentsDeleter.isPresent()) {
            return true;
        }
        RuntimeUnsignedPaymentsDeleter unsignedPaymentsDeleter = maybeUnsignedPaymentsDeleter.get();

        List<UnsignedPayment> unsignedPayments = unsignedPaymentsDeleter.getUnsignedPayments();
        if (unsignedPayments.isEmpty()) {
            return true;
        }

        return unsignedPaymentsDeleter.deleteUnsignedPayments(unsignedPayments);
    }

    public static class FailedToDeleteUnsignedPaymentsException extends RuntimeException {
        public FailedToDeleteUnsignedPaymentsException() {
            super("Failed to delete existing unsigned payments.");
        }
    }
}
