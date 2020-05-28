package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;

public class PaymentAccountClassifier {
    private final List<ClassificationRule<PaymentAccountClassification>> rules;

    public PaymentAccountClassifier(List<ClassificationRule<PaymentAccountClassification>> rules) {
        this.rules = rules;
    }

    public PaymentAccountClassification classifyAsPaymentAccount(
            Provider provider, Account account) {
        // TODO process rules according to the algorithm/strategy
        Stream<ClassificationRule<PaymentAccountClassification>> applicableRules =
                Optional.ofNullable(rules).orElse(Collections.emptyList()).stream()
                        .filter(r -> r.isApplicable(provider));

        return PaymentAccountClassification.UNDETERMINED;
    }
}
