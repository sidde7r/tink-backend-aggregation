package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationState;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.BulkPaymentTestAgent;

public class BulkPaymentInitiationGetStatusTimeoutTest {
    @Test
    public void testGetStatusTimeout() {
        // Set up the agent to successfully register and sign the payment but to always return
        // PENDING when polling for status. The payment initiation will be constructed with a very
        // short polling timeout. We expect the final state of the payment to be PENDING.
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        List.of(
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .noError()
                                        .build()),
                        List.of(
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .status(PaymentStatus.PENDING)
                                        .noDebtor()
                                        .build()),
                        List.of(
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .status(PaymentStatus.PENDING)
                                        .noDebtor()
                                        .build()));

        List<Payment> payments = List.of(PaymentInitiationTestHelper.PAYMENT_1);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(
                        agent, Duration.ofMillis(1), payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(agent.getPaymentsAskedToRegister().toArray()));
        MatcherAssert.assertThat(
                payments, Matchers.containsInAnyOrder(agent.getPaymentsAskedToSign().toArray()));
        MatcherAssert.assertThat(
                new HashSet<>(payments),
                Matchers.containsInAnyOrder(agent.getPaymentsAskedToGetStatus().toArray()));

        // Assert that the final report contain the expected final statuses and/or errors.
        List<PaymentInitiationState> expectedFinalPaymentStates =
                List.of(
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                .status(PaymentStatus.PENDING)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }
}
