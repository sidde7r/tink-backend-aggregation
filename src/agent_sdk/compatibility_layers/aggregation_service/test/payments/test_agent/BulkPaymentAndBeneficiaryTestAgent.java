package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import se.tink.agent.sdk.payments.beneficiary.BeneficiariesFetcher;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.features.beneficiary.FetchBeneficiaries;
import se.tink.agent.sdk.payments.features.beneficiary.RegisterBeneficiaryGeneric;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;

public class BulkPaymentAndBeneficiaryTestAgent
        implements FetchBeneficiaries, RegisterBeneficiaryGeneric, InitiateBulkPaymentGeneric {
    private final PaymentsTestExecutionReport report;
    private final PaymentsTestContract contract;

    public BulkPaymentAndBeneficiaryTestAgent(
            PaymentsTestExecutionReport report, PaymentsTestContract contract) {
        this.report = report;
        this.contract = contract;
    }

    @Override
    public BeneficiariesFetcher beneficiariesFetcher() {
        return new TestAgentBeneficiariesFetcher(this.report, this.contract);
    }

    @Override
    public GenericBeneficiaryRegistrator beneficiaryRegistrator() {
        return new TestAgentBeneficiaryRegistrator(this.report, this.contract);
    }

    @Override
    public GenericBulkPaymentInitiator bulkPaymentInitiator() {
        return new TestAgentBulkPaymentInitiator(this.report, this.contract);
    }
}
