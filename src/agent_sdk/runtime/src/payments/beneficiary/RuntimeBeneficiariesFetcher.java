package se.tink.agent.runtime.payments.beneficiary;

import java.util.List;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.payments.beneficiary.BeneficiariesFetcher;
import se.tink.libraries.account.AccountIdentifier;

public class RuntimeBeneficiariesFetcher {
    private final BeneficiariesFetcher agentBeneficiariesFetcher;

    public RuntimeBeneficiariesFetcher(BeneficiariesFetcher agentBeneficiariesFetcher) {
        this.agentBeneficiariesFetcher = agentBeneficiariesFetcher;
    }

    public List<Beneficiary> fetchPaymentBeneficiariesFor(AccountIdentifier accountIdentifier) {
        return this.agentBeneficiariesFetcher.fetchPaymentBeneficiariesFor(accountIdentifier);
    }
}
