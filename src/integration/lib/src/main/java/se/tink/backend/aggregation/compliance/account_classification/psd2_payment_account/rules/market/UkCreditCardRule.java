package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.market;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.libraries.enums.MarketCode;

public class UkCreditCardRule
        implements AccountClassificationRule<Psd2PaymentAccountClassificationResult> {
    @Override
    public boolean isApplicable(Provider provider) {
        return MarketCode.GB.toString().equals(provider.getMarket());
    }

    @Override
    public Psd2PaymentAccountClassificationResult classify(Provider provider, Account account) {
        if (account.getType() == AccountTypes.CREDIT_CARD) {
            return Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT;
        }
        return Psd2PaymentAccountClassificationResult.UNDETERMINED_PAYMENT_ACCOUNT;
    }
}
