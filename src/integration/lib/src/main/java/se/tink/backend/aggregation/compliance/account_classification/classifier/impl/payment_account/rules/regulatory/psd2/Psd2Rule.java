package se.tink.backend.aggregation.compliance.account_classification.classifier.impl.payment_account.rules.regulatory.psd2;

import java.util.Objects;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_classification.PaymentAccountClassification;
import se.tink.backend.aggregation.compliance.account_classification.classifier.impl.ClassificationRule;

public class Psd2Rule implements ClassificationRule<PaymentAccountClassification> {
    @Override
    public boolean isApplicable(Provider provider) {
        // We only want to classify accounts that are produced by a Provider in a PSD2 market.
        return Psd2Markets.isPsd2Market(provider.getMarket());
    }

    private boolean isAnyCapabilityYes(AccountCapabilities accountCapabilities) {
        return accountCapabilities.getCanMakeAndReceiveTransfer() == AccountCapabilities.Answer.YES
                || accountCapabilities.getCanPlaceFunds() == AccountCapabilities.Answer.YES
                || accountCapabilities.getCanWithdrawFunds() == AccountCapabilities.Answer.YES;
    }

    private boolean areAllCapabilitiesNo(AccountCapabilities accountCapabilities) {
        return accountCapabilities.getCanMakeAndReceiveTransfer() == AccountCapabilities.Answer.NO
                && accountCapabilities.getCanPlaceFunds() == AccountCapabilities.Answer.NO
                && accountCapabilities.getCanWithdrawFunds() == AccountCapabilities.Answer.NO;
    }

    // Note: This rule is ONLY applicable for providers in a PSD2 market (see `isApplicable`).
    // The classification works as follows:
    // 1. Is provider OpenBanking ==> PAYMENT_ACCOUNT
    // 2. Account lacks capabilities ==>  UNDETERMINED
    // 3. Is any capability YES ==> PAYMENT_ACCOUNT
    // 4. Are all capabilities NO ==> NON_PAYMENT_ACCOUNT
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

        if (isAnyCapabilityYes(accountCapabilities)) {
            return PaymentAccountClassification.PAYMENT_ACCOUNT;
        }

        if (areAllCapabilitiesNo(accountCapabilities)) {
            return PaymentAccountClassification.NON_PAYMENT_ACCOUNT;
        }

        return PaymentAccountClassification.UNDETERMINED;
    }
}
