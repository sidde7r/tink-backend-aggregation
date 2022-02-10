package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.List;
import org.junit.Ignore;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;

@Ignore
// This is an agent implementation to test bulk payment initiation. The behaviour of the process is
// controlled via the constructor arguments, it's possible to query which payments have been
// processed via the getter methods.
public class BulkPaymentTestAgent implements InitiateBulkPaymentGeneric {
    private final List<BulkPaymentRegisterResult> registerResults;
    private final List<BulkPaymentSignResult> signResults;
    private final List<BulkPaymentSignResult> statusResults;

    private final PaymentsTestExecutionReport report;

    public BulkPaymentTestAgent(
            PaymentsTestExecutionReport report,
            List<BulkPaymentRegisterResult> registerResults,
            List<BulkPaymentSignResult> signResults,
            List<BulkPaymentSignResult> statusResults) {
        this.report = report;
        this.registerResults = registerResults;
        this.signResults = signResults;
        this.statusResults = statusResults;
    }

    @Override
    public GenericBulkPaymentInitiator bulkPaymentInitiator() {
        return new TestAgentBulkPaymentInitiator(
                this.report, this.registerResults, this.signResults, this.statusResults);
    }
}
