package se.tink.backend.aggregation.compliance.account_classification.classifier;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.PaymentAccountRulesProvider;

public class AccountClassifier {
    private final PaymentAccountClassifier paymentAccountClassifier;

    public AccountClassifier() {
        paymentAccountClassifier =
                new PaymentAccountClassifier(PaymentAccountRulesProvider.getRules());
    }

    public PaymentAccountClassification classifyForPaymentAccount(
            Provider provider, Account account) {
        return paymentAccountClassifier.classifyForPaymentAccount(provider, account);
    }
}
