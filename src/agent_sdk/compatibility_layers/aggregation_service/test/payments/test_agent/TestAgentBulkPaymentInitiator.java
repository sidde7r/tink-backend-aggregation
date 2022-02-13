package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
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
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public class TestAgentBulkPaymentInitiator implements GenericBulkPaymentInitiator {
    private static final String BANK_BASKET_REFERENCE = "bank-basket-ref";

    private final PaymentsTestExecutionReport report;
    private final PaymentsTestContract contract;

    private boolean isFirstGetStatusPoll = true;

    public TestAgentBulkPaymentInitiator(
            PaymentsTestExecutionReport report, PaymentsTestContract contract) {
        this.report = report;
        this.contract = contract;
    }

    @Override
    public BulkPaymentRegisterBasketResult registerPayments(List<Payment> payments) {

        report.addPaymentsToRegister(payments);

        ArrayList<BulkPaymentRegisterResult> results =
                new ArrayList<>(contract.getRegisterPaymentResults());

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
                .startStep(new TestAgentBulkPaymentSignStep(report, contract))
                .build();
    }

    @Override
    public BulkPaymentSignBasketResult getSignStatus(BulkPaymentSigningBasket basket) {
        // Ensure the bank basket reference is identical to the one reported in the register
        // step.
        Assert.assertEquals(BANK_BASKET_REFERENCE, basket.getBankBasketReference());

        report.addPaymentsToGetSignStatus(
                basket.getPaymentReferences().stream()
                        .map(PaymentReference::getPayment)
                        .collect(Collectors.toList()));

        List<BulkPaymentSignResult> results =
                new ArrayList<>(contract.getPaymentSignStatusResults());

        // Shuffle the list to ensure that lists are not compared using `.equals()` (which
        // requires the lists to have the same order).
        Collections.shuffle(results);

        // Simulate a potential state transition from PENDING -> actual status by always
        // returning PENDING the first time.
        if (isFirstGetStatusPoll) {
            results =
                    results.stream().map(this::switchToPendingStatus).collect(Collectors.toList());

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

    private static class TestAgentBulkPaymentSignStep extends BulkPaymentSignStep {
        private final PaymentsTestExecutionReport report;
        private final PaymentsTestContract contract;

        public TestAgentBulkPaymentSignStep(
                PaymentsTestExecutionReport report, PaymentsTestContract contract) {
            this.report = report;
            this.contract = contract;
        }

        @Override
        public InteractiveStepResponse<BulkPaymentSignBasketResult> execute(
                StepRequest<BulkPaymentSigningBasket> request) {

            BulkPaymentSigningBasket basket = request.getStepArgument();

            // Ensure the bank basket reference is identical to the one reported in the register
            // step.
            Assert.assertEquals(BANK_BASKET_REFERENCE, basket.getBankBasketReference());

            report.addPaymentsToSign(
                    basket.getPaymentReferences().stream()
                            .map(PaymentReference::getPayment)
                            .collect(Collectors.toList()));

            List<BulkPaymentSignResult> results = new ArrayList<>(contract.getSignPaymentResults());

            // Shuffle the list to ensure that lists are not compared using `.equals()` (which
            // requires the lists to have the same order).
            Collections.shuffle(results);

            return InteractiveStepResponse.done(
                    BulkPaymentSignBasketResult.builder().paymentResults(results).build());
        }
    }
}
