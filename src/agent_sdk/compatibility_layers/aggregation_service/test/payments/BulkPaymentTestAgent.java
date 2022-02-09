package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Ignore;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.BulkPaymentRegisterBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder.BulkPaymentSignResultBuildDebtor;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.bulk.steppable_execution.BulkPaymentSignFlow;
import se.tink.agent.sdk.payments.bulk.steppable_execution.BulkPaymentSignStep;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

@Ignore
// This is an agent implementation to test bulk payment initiation. The behaviour of the process is
// controlled via the constructor arguments, it's possible to query which payments have been
// processed via the getter methods.
public class BulkPaymentTestAgent implements InitiateBulkPaymentGeneric {
    private static final String BANK_BASKET_REFERENCE = "bank-basket-ref";

    private final List<BulkPaymentRegisterResult> registerResults;
    private final List<BulkPaymentSignResult> signResults;
    private final List<BulkPaymentSignResult> statusResults;

    private final List<Payment> paymentsAskedToRegister = new ArrayList<>();
    private final List<Payment> paymentsAskedToSign = new ArrayList<>();
    private final Set<Payment> paymentsAskedToGetStatus = new HashSet<>();

    public BulkPaymentTestAgent(
            List<BulkPaymentRegisterResult> registerResults,
            List<BulkPaymentSignResult> signResults,
            List<BulkPaymentSignResult> statusResults) {
        this.registerResults = registerResults;
        this.signResults = signResults;
        this.statusResults = statusResults;
    }

    public List<Payment> getPaymentsAskedToRegister() {
        return paymentsAskedToRegister;
    }

    public List<Payment> getPaymentsAskedToSign() {
        return paymentsAskedToSign;
    }

    public Set<Payment> getPaymentsAskedToGetStatus() {
        return paymentsAskedToGetStatus;
    }

    @Override
    public GenericBulkPaymentInitiator bulkPaymentInitiator() {
        return new TestAgentBulkPaymentInitiator(
                this.registerResults,
                this.signResults,
                this.statusResults,
                this.paymentsAskedToRegister,
                this.paymentsAskedToSign,
                this.paymentsAskedToGetStatus);
    }

    private static class TestAgentBulkPaymentInitiator implements GenericBulkPaymentInitiator {
        private final List<BulkPaymentRegisterResult> registerResults;
        private final List<BulkPaymentSignResult> signResults;
        private final List<BulkPaymentSignResult> statusResults;

        private final List<Payment> paymentsAskedToRegister;
        private final List<Payment> paymentsAskedToSign;
        private final Set<Payment> paymentsAskedToGetStatus;

        private boolean isFirstGetStatusPoll = true;

        public TestAgentBulkPaymentInitiator(
                List<BulkPaymentRegisterResult> registerResults,
                List<BulkPaymentSignResult> signResults,
                List<BulkPaymentSignResult> statusResults,
                List<Payment> paymentsAskedToRegister,
                List<Payment> paymentsAskedToSign,
                Set<Payment> paymentsAskedToGetStatus) {
            this.registerResults = registerResults;
            this.signResults = signResults;
            this.statusResults = statusResults;
            this.paymentsAskedToRegister = paymentsAskedToRegister;
            this.paymentsAskedToSign = paymentsAskedToSign;
            this.paymentsAskedToGetStatus = paymentsAskedToGetStatus;
        }

        @Override
        public BulkPaymentRegisterBasketResult registerPayments(List<Payment> payments) {

            paymentsAskedToRegister.addAll(payments);

            ArrayList<BulkPaymentRegisterResult> results = new ArrayList<>(registerResults);

            // Shuffle the list to ensure that lists are not compared using `.equals()` (which
            // requires the lists to have the same order).
            Collections.shuffle(results);

            return BulkPaymentRegisterBasketResult.builder()
                    .basketReference(BANK_BASKET_REFERENCE)
                    .paymentResults(results)
                    .build();
        }

        @Override
        public BulkPaymentSignFlow getSignFlow() {
            return BulkPaymentSignFlow.builder()
                    .startStep(new SuccessfulSignStep(signResults, paymentsAskedToSign))
                    .build();
        }

        @Override
        public BulkPaymentSignBasketResult getSignStatus(BulkPaymentSigningBasket basket) {
            // Ensure the bank basket reference is identical to the one reported in the register
            // step.
            Assert.assertEquals(BANK_BASKET_REFERENCE, basket.getBankBasketReference());

            paymentsAskedToGetStatus.addAll(
                    basket.getPaymentReferences().stream()
                            .map(PaymentReference::getPayment)
                            .collect(Collectors.toList()));

            List<BulkPaymentSignResult> results = new ArrayList<>(statusResults);

            // Shuffle the list to ensure that lists are not compared using `.equals()` (which
            // requires the lists to have the same order).
            Collections.shuffle(results);

            // Simulate a potential state transition from PENDING -> actual status by always
            // returning PENDING the first time.
            if (isFirstGetStatusPoll) {
                results =
                        results.stream()
                                .map(this::switchToPendingStatus)
                                .collect(Collectors.toList());

                isFirstGetStatusPoll = false;
            }

            return BulkPaymentSignBasketResult.builder().paymentResults(results).build();
        }

        private BulkPaymentSignResult switchToPendingStatus(BulkPaymentSignResult originalResult) {
            BulkPaymentSignResultBuildDebtor builder =
                    BulkPaymentSignResult.builder()
                            .reference(originalResult.getReference())
                            .status(PaymentStatus.PENDING);
            if (originalResult.getDebtor().isPresent()) {
                return builder.debtor(originalResult.getDebtor().get()).build();
            }
            return builder.noDebtor().build();
        }
    }

    private static class SuccessfulSignStep extends BulkPaymentSignStep {
        private final List<BulkPaymentSignResult> signResults;
        private final List<Payment> paymentsAskedToSign;

        public SuccessfulSignStep(
                List<BulkPaymentSignResult> signResults, List<Payment> paymentsAskedToSign) {
            this.signResults = signResults;
            this.paymentsAskedToSign = paymentsAskedToSign;
        }

        @Override
        public InteractiveStepResponse<BulkPaymentSignBasketResult> execute(
                StepRequest<BulkPaymentSigningBasket> request) {

            BulkPaymentSigningBasket basket = request.getStepArgument();

            // Ensure the bank basket reference is identical to the one reported in the register
            // step.
            Assert.assertEquals(BANK_BASKET_REFERENCE, basket.getBankBasketReference());

            paymentsAskedToSign.addAll(
                    basket.getPaymentReferences().stream()
                            .map(PaymentReference::getPayment)
                            .collect(Collectors.toList()));

            List<BulkPaymentSignResult> results = new ArrayList<>(this.signResults);

            // Shuffle the list to ensure that lists are not compared using `.equals()` (which
            // requires the lists to have the same order).
            Collections.shuffle(results);

            return InteractiveStepResponse.done(
                    BulkPaymentSignBasketResult.builder().paymentResults(results).build());
        }
    }
}
