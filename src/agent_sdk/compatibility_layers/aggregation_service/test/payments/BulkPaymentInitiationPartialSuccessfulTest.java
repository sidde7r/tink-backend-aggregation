package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.BeneficiaryError;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.PaymentError;
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

public class BulkPaymentInitiationPartialSuccessfulTest {

    @Test
    public void testPartialSuccessful() {
        // Set up the agent to partially succeed at every step:
        //   - Register beneficiary will fail 1.
        //   - Sign beneficiary will fail 1.
        //   - Register payment will fail 1.
        //   - Sign payment will fail 1 (the rest go into PENDING).
        //   - GetSignStatus will fail 1 (the rest go into INITIATED_AND_EXECUTED).
        //
        // We expect that each of the above-mentioned steps will have processed fewer and fewer
        // payments, due to earlier steps failing a payment.
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
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_4_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_5_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .registerBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_6_BENEFICIARY,
                                        BeneficiaryRegisterResult.builder()
                                                .noError()
                                                .noBankReference()
                                                .build())
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                                        BeneficiaryState.error(
                                                BeneficiaryError.BENEFICIARY_INVALID))
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY,
                                        BeneficiaryState.successful())
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_4_BENEFICIARY,
                                        BeneficiaryState.successful())
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_5_BENEFICIARY,
                                        BeneficiaryState.successful())
                                .signBeneficiaryResult(
                                        PaymentInitiationTestHelper.PAYMENT_6_BENEFICIARY,
                                        BeneficiaryState.successful())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_4_REF)
                                                .noError()
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_5_REF)
                                                .noError()
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_6_REF)
                                                .noError()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_4_REF)
                                                .error(
                                                        PaymentError
                                                                .DEBTOR_ACCOUNT_NOT_PAYMENT_ACCOUNT)
                                                .noDebtor()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_5_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_6_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .paymentSignStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_5_REF)
                                                .error(
                                                        PaymentError
                                                                .PERMISSIONS_NO_PAYMENT_PERMISSION)
                                                .noDebtor()
                                                .build())
                                .paymentSignStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_6_REF)
                                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                                .noDebtor()
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3,
                        PaymentInitiationTestHelper.PAYMENT_4,
                        PaymentInitiationTestHelper.PAYMENT_5,
                        PaymentInitiationTestHelper.PAYMENT_6);

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
                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_4_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_5_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_6_BENEFICIARY),
                Matchers.containsInAnyOrder(
                        executionReport.getBeneficiariesAskedToRegister().toArray()));
        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_2_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_3_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_4_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_5_BENEFICIARY,
                        PaymentInitiationTestHelper.PAYMENT_6_BENEFICIARY),
                Matchers.containsInAnyOrder(
                        executionReport.getBeneficiariesAskedToSign().toArray()));

        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_3,
                        PaymentInitiationTestHelper.PAYMENT_4,
                        PaymentInitiationTestHelper.PAYMENT_5,
                        PaymentInitiationTestHelper.PAYMENT_6),
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToRegister().toArray()));
        MatcherAssert.assertThat(
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_4,
                        PaymentInitiationTestHelper.PAYMENT_5,
                        PaymentInitiationTestHelper.PAYMENT_6),
                Matchers.containsInAnyOrder(executionReport.getPaymentsAskedToSign().toArray()));
        MatcherAssert.assertThat(
                Set.of(
                        PaymentInitiationTestHelper.PAYMENT_5,
                        PaymentInitiationTestHelper.PAYMENT_6),
                Matchers.containsInAnyOrder(
                        executionReport.getPaymentsAskedToGetSignStatus().toArray()));

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
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_3_REF)
                                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_4_REF)
                                .error(PaymentError.DEBTOR_ACCOUNT_NOT_PAYMENT_ACCOUNT)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_5_REF)
                                .error(PaymentError.PERMISSIONS_NO_PAYMENT_PERMISSION)
                                .build(),
                        PaymentInitiationState.builder()
                                .paymentReference(PaymentInitiationTestHelper.PAYMENT_6_REF)
                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                .build());
        MatcherAssert.assertThat(
                expectedFinalPaymentStates,
                Matchers.containsInAnyOrder(
                        paymentInitiationReport.getFinalPaymentStates().toArray()));
    }
}
