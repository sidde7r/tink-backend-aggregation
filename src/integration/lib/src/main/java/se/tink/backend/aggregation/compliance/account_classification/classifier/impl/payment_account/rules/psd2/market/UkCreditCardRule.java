package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.psd2.market;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.libraries.enums.MarketCode;

public class UkCreditCardRule implements ClassificationRule<PaymentAccountClassification> {
    @Override
    public boolean isApplicable(Provider provider) {
        return MarketCode.GB.toString().equals(provider.getMarket());
    }

    @Override
    public PaymentAccountClassification classify(Provider provider, Account account) {
        if (account.getType() == AccountTypes.CREDIT_CARD) {
            return PaymentAccountClassification.PAYMENT_ACCOUNT;
        }
        return PaymentAccountClassification.UNDETERMINED;
    }
}
