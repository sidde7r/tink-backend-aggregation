package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.List;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.models.payments.payment.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report.PaymentInitiationReport;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.BulkPaymentTestAgent;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestContract;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestExecutionReport;

public class BulkPaymentInitiationMissingResultsTest {
    @Test(expected = PaymentInitiationReport.InconsistentPaymentStateException.class)
    public void testMissingRegistrationResult() {
        // The agent will only report back two register results when it was asked to register three
        // payments.
        // We expect an exception due to this.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .noError()
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .noError()
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3);

        PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);
    }

    @Test(expected = PaymentInitiationReport.InconsistentPaymentStateException.class)
    public void testMissingSignResult() {
        // The agent will only report back two sign results when it was asked to sign three
        // payments.
        // We expect an exception due to this.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .noError()
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .noError()
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .noError()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3);

        PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);
    }

    @Test(expected = PaymentInitiationReport.InconsistentPaymentStateException.class)
    public void testMissingStatusResult() {
        // The agent will only report back two status results when it was asked to get three payment
        // statuses.
        // We expect an exception due to this.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .noError()
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .noError()
                                                .build())
                                .registerPaymentResult(
                                        BulkPaymentRegisterResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .noError()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .signPaymentResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_3_REF)
                                                .status(PaymentStatus.PENDING)
                                                .noDebtor()
                                                .build())
                                .paymentSignStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_1_REF)
                                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                                .noDebtor()
                                                .build())
                                .paymentSignStatusResult(
                                        BulkPaymentSignResult.builder()
                                                .reference(
                                                        PaymentInitiationTestHelper.PAYMENT_2_REF)
                                                .status(PaymentStatus.INITIATED_AND_EXECUTED)
                                                .noDebtor()
                                                .build())
                                .build());

        List<Payment> payments =
                List.of(
                        PaymentInitiationTestHelper.PAYMENT_1,
                        PaymentInitiationTestHelper.PAYMENT_2,
                        PaymentInitiationTestHelper.PAYMENT_3);

        PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);
    }
}
