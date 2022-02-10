package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.Collections;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationState;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.BulkPaymentTestAgent;

public class BulkPaymentInitiationEarlyFailuresTest {

    @Test
    public void testEarlyFailureOnRegister() {
        // Set up the agent to fail all payments on register.
        // The process should not continue to sign.
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        List.of(
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                        .build(),
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                        .error(PaymentError.AMOUNT_LESS_THAN_BANK_LIMIT)
                                        .build()),
                        Collections.emptyList(),
                        Collections.emptyList());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(agent.getPaymentsAskedToRegister().toArray()));
        Assert.assertTrue(agent.getPaymentsAskedToSign().isEmpty());
        Assert.assertTrue(agent.getPaymentsAskedToGetStatus().isEmpty());

        // Assert that the final report contain the expected final statuses and/or errors.
        List<PaymentInitiationState> expectedFinalPaymentStates =
                List.of(
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                .error(PaymentError.AMOUNT_LESS_THAN_BANK_LIMIT)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }

    @Test
    public void testEarlyFailureOnSign() {
        // Set up the agent to fail all payments on sign.
        // The process should not continue to getStatus.
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        List.of(
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .noError()
                                        .build(),
                                BulkPaymentRegisterResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                        .noError()
                                        .build()),
                        List.of(
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                        .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                        .noDebtor()
                                        .build(),
                                BulkPaymentSignResult.builder()
                                        .reference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                        .error(PaymentError.AMOUNT_LESS_THAN_BANK_LIMIT)
                                        .noDebtor()
                                        .build()),
                        Collections.emptyList());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(agent.getPaymentsAskedToRegister().toArray()));
        MatcherAssert.assertThat(
                payments, Matchers.containsInAnyOrder(agent.getPaymentsAskedToSign().toArray()));
        Assert.assertTrue(agent.getPaymentsAskedToGetStatus().isEmpty());

        // Assert that the final report contain the expected final statuses and/or errors.
        List<PaymentInitiationState> expectedFinalPaymentStates =
                List.of(
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                .error(PaymentError.AMOUNT_LESS_THAN_BANK_LIMIT)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }
}
