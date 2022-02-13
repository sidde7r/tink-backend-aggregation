package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.BeneficiaryError;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationState;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.BulkPaymentAndBeneficiaryTestAgent;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestContract;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestExecutionReport;

public class BulkPaymentInitiationEarlyFailuresTest {

    @Test
    public void testEarlyFailureOnRegisterBeneficiary() {
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();

        BulkPaymentAndBeneficiaryTestAgent agent =
                new BulkPaymentAndBeneficiaryTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_1_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .error(
                                                        BeneficiaryError
                                                                .BENEFICIARY_INVALID_ACCOUNT_TYPE)
                                                .build())
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .error(BeneficiaryError.BENEFICIARY_INVALID)
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                List.of(PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER),
                Matchers.containsInAnyOrder(
                        executionReport.getAccountsAskedToFetchBeneficiariesFor().toArray()));

        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY),
                Matchers.containsInAnyOrder(
                        executionReport.getBeneficiariesAskedToRegister().toArray()));

        Assert.assertTrue(executionReport.getBeneficiariesAskedToSign().isEmpty());
        Assert.assertTrue(executionReport.getPaymentsAskedToRegister().isEmpty());
        Assert.assertTrue(executionReport.getPaymentsAskedToSign().isEmpty());
        Assert.assertTrue(executionReport.getPaymentsAskedToGetSignStatus().isEmpty());

        // Assert that the final report contain the expected final statuses and/or errors.
        List<PaymentInitiationState> expectedFinalPaymentStates =
                List.of(
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                .error(BeneficiaryError.BENEFICIARY_INVALID_ACCOUNT_TYPE)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                .error(BeneficiaryError.BENEFICIARY_INVALID)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }

    @Test
    public void testEarlyFailureOnSignBeneficiary() {
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();

        BulkPaymentAndBeneficiaryTestAgent agent =
                new BulkPaymentAndBeneficiaryTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_1_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_1_BENEFICIARY,
                                        BeneficiaryState.error(
                                                BeneficiaryError.BENEFICIARY_INVALID_ACCOUNT_TYPE))
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                                        BeneficiaryState.error(
                                                BeneficiaryError.BENEFICIARY_INVALID))
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                List.of(PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER),
                Matchers.containsInAnyOrder(
                        executionReport.getAccountsAskedToFetchBeneficiariesFor().toArray()));

        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY),
                Matchers.containsInAnyOrder(
                        executionReport.getBeneficiariesAskedToRegister().toArray()));

        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY),
                Matchers.containsInAnyOrder(
                        executionReport.getBeneficiariesAskedToSign().toArray()));

        Assert.assertTrue(executionReport.getPaymentsAskedToRegister().isEmpty());
        Assert.assertTrue(executionReport.getPaymentsAskedToSign().isEmpty());
        Assert.assertTrue(executionReport.getPaymentsAskedToGetSignStatus().isEmpty());

        // Assert that the final report contain the expected final statuses and/or errors.
        List<PaymentInitiationState> expectedFinalPaymentStates =
                List.of(
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_1_REF)
                                .error(BeneficiaryError.BENEFICIARY_INVALID_ACCOUNT_TYPE)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_2_REF)
                                .error(BeneficiaryError.BENEFICIARY_INVALID)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }

    @Test
    public void testEarlyFailureOnRegisterPayment() {
        // Set up the agent to fail all payments on register.
        // The process should not continue to sign.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentAndBeneficiaryTestAgent agent =
                new BulkPaymentAndBeneficiaryTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .fetchBeneficiaryResult(
                                        PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER,
                                        List.of(
                                                PaymentInitiationTestHelper.BENEFICIARY_ALICE,
                                                PaymentInitiationTestHelper.BENEFICIARY_BOB))
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
                                                .error(PaymentError.AMOUNT_LESS_THAN_BANK_LIMIT)
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                List.of(PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER),
                Matchers.containsInAnyOrder(
                        executionReport.getAccountsAskedToFetchBeneficiariesFor().toArray()));
        Assert.assertTrue(executionReport.getBeneficiariesAskedToRegister().isEmpty());
        Assert.assertTrue(executionReport.getBeneficiariesAskedToSign().isEmpty());
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToRegister().toArray()));
        Assert.assertTrue(executionReport.getPaymentsAskedToSign().isEmpty());
        Assert.assertTrue(executionReport.getPaymentsAskedToGetSignStatus().isEmpty());

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
    public void testEarlyFailureOnSignPayment() {
        // Set up the agent to fail all payments on sign.
        // The process should not continue to getStatus.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentAndBeneficiaryTestAgent agent =
                new BulkPaymentAndBeneficiaryTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .fetchBeneficiaryResult(
                                        PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER,
                                        List.of(
                                                PaymentInitiationTestHelper.BENEFICIARY_ALICE,
                                                PaymentInitiationTestHelper.BENEFICIARY_BOB))
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
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                                .noDebtor()
                                                .build())
                                .signResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .error(PaymentError.AMOUNT_LESS_THAN_BANK_LIMIT)
                                                .noDebtor()
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2);

        PaymentInitiationReport paymentInitiationReport =
                PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);

        // Assert that the agent processed the expected payments at every step.
        MatcherAssert.assertThat(
                List.of(PaymentInitiationTestHelper.DEBTOR_1_ACCOUNT_IDENTIFIER),
                Matchers.containsInAnyOrder(
                        executionReport.getAccountsAskedToFetchBeneficiariesFor().toArray()));
        Assert.assertTrue(executionReport.getBeneficiariesAskedToRegister().isEmpty());
        Assert.assertTrue(executionReport.getBeneficiariesAskedToSign().isEmpty());
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToRegister().toArray()));
        MatcherAssert.assertThat(
                payments,
                Matchers.containsInAnyOrder(executionReport.getPaymentsAskedToSign().toArray()));
        Assert.assertTrue(executionReport.getPaymentsAskedToGetSignStatus().isEmpty());

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
