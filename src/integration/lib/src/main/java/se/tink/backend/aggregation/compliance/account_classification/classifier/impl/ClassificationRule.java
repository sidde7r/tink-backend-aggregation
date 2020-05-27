package se.tink.backend.aggregation.compliance.account_classification.classifier.impl;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;

public interface ClassificationRule<ClassificationResult> {
    boolean isApplicable(Provider provider);

    ClassificationResult classify(Provider provider, Account account);
}
