package se.tink.backend.aggregation.compliance.account_classification.classifier;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.PaymentAccountRulesProvider;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class AccountClassifier {
    private final PaymentAccountClassifier paymentAccountClassifier;

    public AccountClassifier(MetricRegistry metricRegistry, Provider provider) {
        paymentAccountClassifier =
                new PaymentAccountClassifier(
                        PaymentAccountRulesProvider.getRules(), metricRegistry, provider);
    }

    public PaymentAccountClassification classifyAsPaymentAccount(Account account) {
        return paymentAccountClassifier.classifyAsPaymentAccount(account);
    }
}
