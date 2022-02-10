package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationState;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.BulkPaymentTestAgent;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestContract;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestExecutionReport;

public class BulkPaymentInitiationPartialSuccessfulTest {

    @Test
    public void testPartialSuccessful() {
        // Set up the agent to partially succeed at every step:
        //   - Registration will fail 1.
        //   - Signing will fail 1 (the rest go into PENDING).
        //   - GetStatus will fail 1 (the rest go into INITIATED_AND_EXECUTED).
        //
        // We expect that each of the above-mentioned steps will have processed fewer and fewer
        // payments, due to earlier steps failing a payment.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .registerResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                                .build())
                                .registerResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .noError()
                                                .build())
                                .registerResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .noError()
                                                .build())
                                .registerResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_4_REF)
                                                .noError()
                                                .build())
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .error(
                                                        PaymentError
                                                                .DEBTOR_ACCOUNT_NOT_PAYMENT_ACCOUNT)
                                                .noDebtor()
                                                .build())
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_4_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .error(
                                                        PaymentError
                                                                .PERMISSIONS_NO_PAYMENT_PERMISSION)
                                                .noDebtor()
                                                .build())
                                .signStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_4_REF)
                                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                                .noDebtor()
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3,
                        PaymentInitiationTestHelper.PAYMENT_4);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToRegister().toArray()));
        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3,
                        PaymentInitiationTestHelper.PAYMENT_4),
                Matchers.containsInAnyOrder(executionReport.getPaymentsAskedToSign().toArray()));
        MatcherAssert.assertThat(
                Set.of(
                        PaymentInitiationTestHelper.PAYMENT_3,
                        PaymentInitiationTestHelper.PAYMENT_4),
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToGetSignStatus().toArray()));

        // Assert that the final report contain the expected final statuses and/or errors.
        List<PaymentInitiationState> expectedFinalPaymentStates =
                List.of(
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                .error(PaymentError.DEBTOR_ACCOUNT_NOT_PAYMENT_ACCOUNT)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_3_REF)
                                .error(PaymentError.PERMISSIONS_NO_PAYMENT_PERMISSION)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_4_REF)
                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }
}
