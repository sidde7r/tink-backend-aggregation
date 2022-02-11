package se.tink.agent.agents.example.payments;

import java.util.List;
import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.payments.beneficiary.BeneficiariesFetcher;
import se.tink.libraries.account.AccountIdentifier;

public class ExampleBeneficiariesFetcher implements BeneficiariesFetcher {
    @Override
    public List<Beneficiary> fetchPaymentBeneficiariesFor(
            AccountIdentifier debtorAccountIdentifier) {
        return null;
    }
}
