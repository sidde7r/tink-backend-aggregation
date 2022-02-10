package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import org.junit.Ignore;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;

@Ignore
// This is an agent implementation to test bulk payment initiation. The behaviour of the process is
// controlled via the constructor arguments, it's possible to query which payments have been
// processed via the getter methods.
public class BulkPaymentTestAgent implements InitiateBulkPaymentGeneric {
    private final PaymentsTestExecutionReport report;
    private final PaymentsTestContract contract;

    public BulkPaymentTestAgent(PaymentsTestExecutionReport report, PaymentsTestContract contract) {
        this.report = report;
        this.contract = contract;
    }

    @Override
    public GenericBulkPaymentInitiator bulkPaymentInitiator() {
        return new TestAgentBulkPaymentInitiator(this.report, this.contract);
    }
}
