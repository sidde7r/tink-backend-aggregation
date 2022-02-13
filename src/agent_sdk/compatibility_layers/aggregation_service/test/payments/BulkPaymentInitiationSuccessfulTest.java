package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.HashSet;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationState;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.BulkPaymentAndBeneficiaryTestAgent;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestContract;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestExecutionReport;

public class BulkPaymentInitiationSuccessfulTest {

    @Test
    public void testAllSuccessful() {
        // Set up the agent to:
        //  - Fetch 1 beneficiary (for payment 1)
        //  - Successfully register and sign beneficiaries for payment 2 and 3
        //  - Successfully register, sign (with PENDING status) and getSignStatus (with
        // INITIATED_AND_EXECUTED status) for all payments.
        // The final status for all payments should be INITIATED_AND_EXECUTED.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentAndBeneficiaryTestAgent agent =
                new BulkPaymentAndBeneficiaryTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .fetchBeneficiaryResult(
                                        PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER,
                                        List.of(PaymentInitiationTestHelper.BENEFICIARY_ALICE))
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                                        BeneficiaryState.successful())
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY,
                                        BeneficiaryState.successful())
                                .registerResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .noError()
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
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                                .noDebtor()
                                                .build())
                                .signStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                                .noDebtor()
                                                .build())
                                .signStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                                .noDebtor()
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                List.of(PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER),
                Matchers.containsInAnyOrder(
                        executionReport.getAccountsAskedToFetchBeneficiariesFor().toArray()));
        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY),
                Matchers.containsInAnyOrder(
                        executionReport.getBeneficiariesAskedToRegister().toArray()));

        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY),
                Matchers.containsInAnyOrder(
                        executionReport.getBeneficiariesAskedToSign().toArray()));

        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToRegister().toArray()));
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(executionReport.getPaymentsAskedToSign().toArray()));
        MatcherAssert.assertThat(
                new HashSet<>(payments),
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToGetSignStatus().toArray()));

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
