package se.tink.backend.aggregation.compliance.account_classification;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;

public interface AccountClassificationRule<ClassificationResult> {
    boolean isApplicable(Provider provider);

    ClassificationResult classify(Provider provider, Account account);
}
