package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.common;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;

public class CheckingAccountRule
        implements AccountClassificationRule<Psd2PaymentAccountClassificationResult> {
    @Override
    public boolean isApplicable(Provider provider) {
        // We only want to classify accounts that are produced by a Provider in a PSD2 market.
        return Psd2Markets.isPsd2Market(provider.getMarket());
    }

    @Override
    public Psd2PaymentAccountClassificationResult classify(Provider provider, Account account) {
        if (account.getType() == AccountTypes.CHECKING) {
            return Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT;
        }
        return Psd2PaymentAccountClassificationResult.UNDETERMINED_PAYMENT_ACCOUNT;
    }
}
