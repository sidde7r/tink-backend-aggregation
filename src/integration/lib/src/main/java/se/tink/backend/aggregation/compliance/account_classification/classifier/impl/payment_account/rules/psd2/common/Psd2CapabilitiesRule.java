package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.psd2.common;

import java.util.Objects;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.psd2.Psd2Markets;

public class Psd2CapabilitiesRule implements ClassificationRule<PaymentAccountClassification> {
    @Override
    public boolean isApplicable(Provider provider) {
        // We only want to classify accounts that are produced by a Provider in a PSD2 market.
        return Psd2Markets.isPsd2Market(provider.getMarket());
    }

    private boolean areAllCapabilitiesYes(AccountCapabilities accountCapabilities) {
        return accountCapabilities.getCanMakeAndReceiveTransfer() == AccountCapabilities.Answer.YES
                && accountCapabilities.getCanPlaceFunds() == AccountCapabilities.Answer.YES
                && accountCapabilities.getCanWithdrawFunds() == AccountCapabilities.Answer.YES;
    }

    private boolean isAnyCapabilityNo(AccountCapabilities accountCapabilities) {
        return accountCapabilities.getCanMakeAndReceiveTransfer() == AccountCapabilities.Answer.NO
                || accountCapabilities.getCanPlaceFunds() == AccountCapabilities.Answer.NO
                || accountCapabilities.getCanWithdrawFunds() == AccountCapabilities.Answer.NO;
    }

    // Note: This rule is ONLY applicable for providers in a PSD2 market (see `isApplicable`).
    // The classification works as follows:
    // 1. Is provider OpenBanking ==> PAYMENT_ACCOUNT
    // 2. Account lacks capabilities ==>  UNDETERMINED
    // 3. Are all capabilities YES ==> PAYMENT_ACCOUNT
    // 4. Is any capability NO ==> NON_PAYMENT_ACCOUNT
    // 5. ==> UNDETERMINED
    @Override
    public PaymentAccountClassification classify(Provider provider, Account account) {
        if (provider.isOpenBanking()) {
            // Anything produced from an OpenBanking provider is considered a PaymentAccount.
            return PaymentAccountClassification.PAYMENT_ACCOUNT;
        }

        // Evaluate the Account.capabilities based on the PSD2 RTS.
        AccountCapabilities accountCapabilities = account.getCapabilities();
        if (Objects.isNull(accountCapabilities)) {
            return PaymentAccountClassification.UNDETERMINED;
        }

        if (areAllCapabilitiesYes(accountCapabilities)) {
            return PaymentAccountClassification.PAYMENT_ACCOUNT;
        }

        if (isAnyCapabilityNo(accountCapabilities)) {
            return PaymentAccountClassification.NON_PAYMENT_ACCOUNT;
        }

        return PaymentAccountClassification.UNDETERMINED;
    }
}
