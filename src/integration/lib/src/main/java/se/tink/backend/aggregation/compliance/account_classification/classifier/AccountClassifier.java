package se.tink.backend.aggregation.compliance.account_classification.classifier;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.PaymentAccountRulesProvider;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class AccountClassifier {
    private final PaymentAccountClassifier paymentAccountClassifier;

    public AccountClassifier(MetricRegistry metricRegistry) {
        paymentAccountClassifier =
                new PaymentAccountClassifier(
                        PaymentAccountRulesProvider.getRules(), metricRegistry);
    }

    public PaymentAccountClassification classifyAsPaymentAccount(
            Provider provider, Account account) {
        return paymentAccountClassifier.classifyAsPaymentAccount(provider, account);
    }
}
