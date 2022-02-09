package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

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

public class BulkPaymentInitiationSuccessfulTest {

    @Test
    public void testAllSuccessful() {
        // Set up the agent to successfully register, sign (with PENDING status) and getStatus (with
        // INITIATED_AND_EXECUTED status).
        BulkPaymentAgent agent =
                new BulkPaymentAgent(
                        List.of(
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .noError()
                                        .build(),
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                        .noError()
                                        .build(),
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_3_REF)
                                        .noError()
                                        .build()),
                        List.of(
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .status(PaymentStatus.PENDING)
                                        .noDebtor()
                                        .build(),
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                        .status(PaymentStatus.PENDING)
                                        .noDebtor()
                                        .build(),
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_3_REF)
                                        .status(PaymentStatus.PENDING)
                                        .noDebtor()
                                        .build()),
                        List.of(
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                        .noDebtor()
                                        .build(),
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                        .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                        .noDebtor()
                                        .build(),
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_3_REF)
                                        .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                        .noDebtor()
                                        .build()));

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

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
                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_3_REF)
                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }
}
