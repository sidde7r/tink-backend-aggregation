package se.tink.agent.sdk.models.account.builder;

import java.util.List;
import java.util.function.Function;
import se.tink.agent.sdk.models.account.Account;
import se.tink.agent.sdk.models.account.AccountBalance;
import se.tink.agent.sdk.models.account.AccountCapabilities;
import se.tink.agent.sdk.models.account.AccountCredit;
import se.tink.agent.sdk.models.account.AccountHolder;
import se.tink.agent.sdk.models.account.AccountInterestRate;
import se.tink.libraries.account.AccountIdentifier;

public final class AccountBuilder {
    private AccountBuilder() {}

    public static class BankReferenceBuilder<T>
            implements BankReferenceBuildStep<IdentifierBuilder<T>> {
        private final Function<Account, T> intoSpecificAccount;

        public BankReferenceBuilder(Function<Account, T> intoSpecificAccount) {
            this.intoSpecificAccount = intoSpecificAccount;
        }

        @Override
        public IdentifierBuilder<T> bankReference(String bankReference) {
            return null;
        }

        @Override
        public IdentifierBuilder<T> bankReference(Object bankReference) {
            return null;
        }
    }

    public static class BankReferenceForCreditCardBuilder<T>
            implements BankReferenceBuildStep<CreditCardBuilder<T>> {
        private final Function<Account, T> intoSpecificAccount;

        public BankReferenceForCreditCardBuilder(Function<Account, T> intoSpecificAccount) {
            this.intoSpecificAccount = intoSpecificAccount;
        }

        @Override
        public CreditCardBuilder<T> bankReference(String bankReference) {
            return null;
        }

        @Override
        public CreditCardBuilder<T> bankReference(Object bankReference) {
            return null;
        }
    }

    public static class CreditCardBuilder<T>
            implements CreditCardBuildStep<IdentifierAndNameBuilder<T>> {

        @Override
        public IdentifierAndNameBuilder<T> cardNumber(String value) {
            return null;
        }
    }

    public static class IdentifierBuilder<T>
            implements IdentifiersBuildStep<IdentifierAndNameBuilder<T>> {

        @Override
        public IdentifierAndNameBuilder<T> identifier(AccountIdentifier identifier) {
            return null;
        }

        @Override
        public IdentifierAndNameBuilder<T> identifiers(List<AccountIdentifier> identifiers) {
            return null;
        }
    }

    public static class IdentifierAndNameBuilder<T>
            implements IdentifiersBuildStep<IdentifierAndNameBuilder<T>>,
                    NameBuildStep<CapabilitiesBuilder<T>> {

        @Override
        public IdentifierAndNameBuilder<T> identifier(AccountIdentifier identifier) {
            return null;
        }

        @Override
        public IdentifierAndNameBuilder<T> identifiers(List<AccountIdentifier> identifiers) {
            return null;
        }

        @Override
        public CapabilitiesBuilder<T> name(String name) {
            return null;
        }
    }

    public static class CapabilitiesBuilder<T> implements CapabilitiesBuildStep<HoldersBuilder<T>> {
        @Override
        public HoldersBuilder<T> capabilities(AccountCapabilities capabilities) {
            return null;
        }

        @Override
        public HoldersBuilder<T> unknownCapabilities() {
            return null;
        }
    }

    public static class HoldersBuilder<T>
            implements NoHoldersBuildStep<BalancesBuilder<T>>,
                    HoldersBuildStep<HoldersAndBalancesBuilder<T>> {

        @Override
        public BalancesBuilder<T> noHolderInformation() {
            return null;
        }

        @Override
        public HoldersAndBalancesBuilder<T> holder(AccountHolder holder) {
            return null;
        }

        @Override
        public HoldersAndBalancesBuilder<T> holders(List<AccountHolder> holders) {
            return null;
        }
    }

    public static class BalancesBuilder<T>
            implements BalancesBuildStep<BalancesAndCreditBuilder<T>> {

        @Override
        public BalancesAndCreditBuilder<T> balance(AccountBalance balance) {
            return null;
        }

        @Override
        public BalancesAndCreditBuilder<T> balances(List<AccountBalance> balances) {
            return null;
        }
    }

    public static class HoldersAndBalancesBuilder<T>
            implements HoldersBuildStep<HoldersAndBalancesBuilder<T>>,
                    BalancesBuildStep<BalancesAndCreditBuilder<T>> {
        @Override
        public BalancesAndCreditBuilder<T> balance(AccountBalance balance) {
            return null;
        }

        @Override
        public BalancesAndCreditBuilder<T> balances(List<AccountBalance> balances) {
            return null;
        }

        @Override
        public HoldersAndBalancesBuilder<T> holder(AccountHolder holder) {
            return null;
        }

        @Override
        public HoldersAndBalancesBuilder<T> holders(List<AccountHolder> holders) {
            return null;
        }
    }

    public static class BalancesAndCreditBuilder<T>
            implements BalancesBuildStep<BalancesAndCreditBuilder<T>>,
                    NoCreditBuildStep<InterestRateBuilder<T>>,
                    CreditsBuildStep<CreditAndInterestRateBuilder<T>> {

        @Override
        public BalancesAndCreditBuilder<T> balance(AccountBalance balance) {
            return null;
        }

        @Override
        public BalancesAndCreditBuilder<T> balances(List<AccountBalance> balances) {
            return null;
        }

        @Override
        public CreditAndInterestRateBuilder<T> credit(AccountCredit credit) {
            return null;
        }

        @Override
        public CreditAndInterestRateBuilder<T> credits(List<AccountCredit> credits) {
            return null;
        }

        @Override
        public InterestRateBuilder<T> noCredit() {
            return null;
        }
    }

    public static class InterestRateBuilder<T>
            implements NoInterestRateBuildStep<FinalBuilder<T>>,
                    InterestRateBuildStep<FinalBuilder<T>> {

        @Override
        public FinalBuilder<T> interestRate(AccountInterestRate interestRate) {
            return null;
        }

        @Override
        public FinalBuilder<T> noInterestRate() {
            return null;
        }
    }

    public static class CreditAndInterestRateBuilder<T>
            implements CreditsBuildStep<CreditAndInterestRateBuilder<T>>,
                    NoInterestRateBuildStep<FinalBuilder<T>>,
                    InterestRateBuildStep<FinalBuilder<T>> {
        @Override
        public CreditAndInterestRateBuilder<T> credit(AccountCredit credit) {
            return null;
        }

        @Override
        public CreditAndInterestRateBuilder<T> credits(List<AccountCredit> credits) {
            return null;
        }

        @Override
        public FinalBuilder<T> interestRate(AccountInterestRate interestRate) {
            return null;
        }

        @Override
        public FinalBuilder<T> noInterestRate() {
            return null;
        }
    }

    public static class FinalBuilder<T> {
        private Function<Account, T> intoSpecificAccount;

        public T build() {
            return intoSpecificAccount.apply(null);
        }
    }
}
