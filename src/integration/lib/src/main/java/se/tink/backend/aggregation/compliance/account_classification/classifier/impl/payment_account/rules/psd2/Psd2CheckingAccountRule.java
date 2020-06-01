package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.psd2;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;

public class Psd2CheckingAccountRule implements ClassificationRule<PaymentAccountClassification> {
    @Override
    public boolean isApplicable(Provider provider) {
        // We only want to classify accounts that are produced by a Provider in a PSD2 market.
        return Psd2Markets.isPsd2Market(provider.getMarket());
    }

    @Override
    public PaymentAccountClassification classify(Provider provider, Account account) {
        if (account.getType() == AccountTypes.CHECKING) {
            return PaymentAccountClassification.PAYMENT_ACCOUNT;
        }
        return PaymentAccountClassification.UNDETERMINED;
    }
}
