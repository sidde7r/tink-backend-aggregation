package src.agent_sdk.compatibility_layers.aggregation_service.test.payments.test_agent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.payments.beneficiary.BeneficiariesFetcher;
import se.tink.libraries.account.AccountIdentifier;

public class TestAgentBeneficiariesFetcher implements BeneficiariesFetcher {
    private final PaymentsTestExecutionReport report;
    private final PaymentsTestContract contract;

    public TestAgentBeneficiariesFetcher(
            PaymentsTestExecutionReport report, PaymentsTestContract contract) {
        this.report = report;
        this.contract = contract;
    }

    @Override
    public List<Beneficiary> fetchPaymentBeneficiariesFor(
            AccountIdentifier debtorAccountIdentifier) {
        this.report.addAccountToFetchBeneficiariesFor(debtorAccountIdentifier);

        // Return an empty list of beneficiaries if the account was not specified in the contract.
        Map<AccountIdentifier, List<Beneficiary>> fetchBeneficiaryResults =
                this.contract.getFetchBeneficiaryResults();
        return fetchBeneficiaryResults.getOrDefault(
                debtorAccountIdentifier, Collections.emptyList());
    }
}
