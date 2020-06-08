package se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.rules.common;

import java.util.Objects;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_classification.AccountClassificationRule;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;

public class CapabilitiesRule
        implements AccountClassificationRule<Psd2PaymentAccountClassificationResult> {
    @Override
    public boolean isApplicable(Provider provider) {
        // We only want to classify accounts that are produced by a Provider in a PSD2 market.
        return Psd2Markets.isPsd2Market(provider.getMarket());
    }

    private boolean areAllCapabilitiesYes(AccountCapabilities accountCapabilities) {
        return accountCapabilities.getCanMakeDomesticTransfer() == AccountCapabilities.Answer.YES
                && accountCapabilities.getCanReceiveDomesticTransfer()
                        == AccountCapabilities.Answer.YES
                && accountCapabilities.getCanPlaceFunds() == AccountCapabilities.Answer.YES
                && accountCapabilities.getCanWithdrawFunds() == AccountCapabilities.Answer.YES;
    }

    private boolean isAnyCapabilityNo(AccountCapabilities accountCapabilities) {
        return accountCapabilities.getCanMakeDomesticTransfer() == AccountCapabilities.Answer.NO
                || accountCapabilities.getCanReceiveDomesticTransfer()
                        == AccountCapabilities.Answer.NO
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
    public Psd2PaymentAccountClassificationResult classify(Provider provider, Account account) {
        if (provider.isOpenBanking()) {
            // Anything produced from an OpenBanking provider is considered a PaymentAccount.
            return Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT;
        }

        // Evaluate the Account.capabilities based on the PSD2 RTS.
        AccountCapabilities accountCapabilities = account.getCapabilities();
        if (Objects.isNull(accountCapabilities)) {
            return Psd2PaymentAccountClassificationResult.UNDETERMINED_PAYMENT_ACCOUNT;
        }

        if (areAllCapabilitiesYes(accountCapabilities)) {
            return Psd2PaymentAccountClassificationResult.PAYMENT_ACCOUNT;
        }

        if (isAnyCapabilityNo(accountCapabilities)) {
            return Psd2PaymentAccountClassificationResult.NON_PAYMENT_ACCOUNT;
        }

        return Psd2PaymentAccountClassificationResult.UNDETERMINED_PAYMENT_ACCOUNT;
    }
}
