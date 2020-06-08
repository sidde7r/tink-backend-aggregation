package se.tink.backend.aggregation.compliance.account_classification;

import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;

public abstract class AccountClassifier<ClassificationResult> {
    public abstract Optional<ClassificationResult> classify(Provider provider, Account account);

    public boolean isClassifiedAs(
            Provider provider, Account account, ClassificationResult expectedClassification) {
        return classify(provider, account)
                .map(result -> result == expectedClassification)
                .orElse(false);
    }
}
