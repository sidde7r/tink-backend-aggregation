package src.agent_sdk.compatibility_layers.aggregation_service.test.payments;

import java.util.List;
import org.junit.Test;
import se.tink.agent.sdk.models.payments.payment.Payment;
import src.agent_sdk.compatibility_layers.aggregation_service.src.payments.BulkPaymentInitiation;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.BulkPaymentTestAgent;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestContract;
import src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent.PaymentsTestExecutionReport;

public class BulkPaymentInitiationFailedDeleteUnsignedPaymentTest {

    @Test(expected = BulkPaymentInitiation.FailedToDeleteUnsignedPaymentsException.class)
    public void testFailDeleteUnsignedPayments() {
        // The agent will only report back two register results when it was asked to register three
        // payments.
        // We expect an exception due to this.
        PaymentsTestExecutionReport executionReport = new PaymentsTestExecutionReport();
        BulkPaymentTestAgent agent =
                new BulkPaymentTestAgent(
                        executionReport,
                        PaymentsTestContract.builder()
                                .successfullyDeleteUnsignedPayments(false)
                                .build());

        List<Payment> payments = List.of(PaymentInitiationTestHelper.PAYMENT_1);

        PaymentInitiationTestHelper.initiateBulkPayments(agent, payments);
    }
}
