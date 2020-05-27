package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;

public class PaymentAccountClassifier {
    private final List<ClassificationRule<PaymentAccountClassification>> globalRules;
    private final List<ClassificationRule<PaymentAccountClassification>> marketRules;

    public PaymentAccountClassifier(
            List<ClassificationRule<PaymentAccountClassification>> globalRules,
            List<ClassificationRule<PaymentAccountClassification>> marketRules) {
        this.globalRules = globalRules;
        this.marketRules = marketRules;
    }

    public PaymentAccountClassification classifyForPaymentAccount(
            Provider provider, Account account) {
        // TODO process rules according to the algorithm/strategy
        Stream<ClassificationRule<PaymentAccountClassification>> applicableRules =
                Stream.of(globalRules, marketRules)
                        .flatMap(Collection::stream)
                        .filter(r -> r.isApplicable(provider));

        return PaymentAccountClassification.UNDETERMINED;
    }
}
