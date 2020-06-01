package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.psd2;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;

public class Psd2CheckingAccountRule implements ClassificationRule<PaymentAccountClassification> {
    @Override
    public boolean isApplicable(Provider provider) {
        return true;
    }

    @Override
    public PaymentAccountClassification classify(Provider provider, Account account) {
        if (account.getType() == AccountTypes.CHECKING) {
            return PaymentAccountClassification.PAYMENT_ACCOUNT;
        }
        return PaymentAccountClassification.UNDETERMINED;
    }
}
