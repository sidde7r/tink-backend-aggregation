package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;
import se.tink.agent.sdk.payments.features.global_signing_basket.DeleteUnsignedPayments;
import se.tink.agent.sdk.payments.global_signing_basket.UnsignedPaymentsDeleter;

// This is an agent implementation to test bulk payment initiation. The behaviour of the process is
// controlled via the PaymentsTestContract, it's possible to query which payments have been
// processed via the PaymentsTestExecutionReport.
public class BulkPaymentTestAgent implements DeleteUnsignedPayments, InitiateBulkPaymentGeneric {
    private final PaymentsTestExecutionReport report;
    private final PaymentsTestContract contract;

    public BulkPaymentTestAgent(PaymentsTestExecutionReport report, PaymentsTestContract contract) {
        this.report = report;
        this.contract = contract;
    }

    @Override
    public UnsignedPaymentsDeleter unsignedPaymentsDeleter() {
        return new TestAgentUnsignedPaymentsDeleter(this.contract);
    }

    @Override
    public GenericBulkPaymentInitiator bulkPaymentInitiator() {
        return new TestAgentBulkPaymentInitiator(this.report, this.contract);
    }
}
