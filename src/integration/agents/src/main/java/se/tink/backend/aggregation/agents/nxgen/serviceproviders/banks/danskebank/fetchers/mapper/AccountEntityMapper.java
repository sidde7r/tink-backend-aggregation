package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AccountEntityMapper {

    public List<TransactionalAccount> toTinkCheckingAccounts(
            List<String> knownCheckingAccountProducts, List<AccountEntity> accounts) {
        return accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(
                        DanskeBankPredicates.knownCheckingAccountProducts(
                                knownCheckingAccountProducts))
                .map(this::toCheckingAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<TransactionalAccount> toTinkSavingsAccounts(
            DanskeBankConfiguration configuration, List<AccountEntity> accounts) {
        return accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(
                        DanskeBankPredicates.knownSavingsAccountProducts(
                                configuration.getSavingsAccountTypes()))
                .map(account -> toSavingsAccount(configuration, account))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<CreditCardAccount> toTinkCreditCardAccounts(
            DanskeBankConfiguration configuration, List<AccountEntity> accounts) {
        return accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS)
                .map(account -> toCreditCardAccount(configuration, account))
                .collect(Collectors.toList());
    }

    public LoanAccount toLoanAccount(
            DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return LoanAccount.builder(
                        accountEntity.getAccountNoInt(),
                        ExactCurrencyAmount.of(
                                accountEntity.getBalance(), accountEntity.getCurrency()))
                .setAccountNumber(accountEntity.getAccountNoExt())
                .setName(accountEntity.getAccountName())
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .setDetails(
                        isMortgage(configuration, accountEntity)
                                ? LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                                        .setLoanNumber(accountEntity.getAccountNoExt())
                                        .build()
                                : null)
                .build();
    }

    private boolean isMortgage(
            final DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return configuration
                .getLoanAccountTypes()
                .getOrDefault(accountEntity.getAccountProduct(), Loan.Type.OTHER)
                .equals(Loan.Type.MORTGAGE);
    }

    public CreditCardAccount toCreditCardAccount(
            DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return CreditCardAccount.builder(
                        accountEntity.getAccountNoInt(),
                        ExactCurrencyAmount.of(
                                accountEntity.getBalance(), accountEntity.getCurrency()),
                        calculateAvailableCredit(accountEntity))
                .setAccountNumber(accountEntity.getAccountNoExt())
                .setName(accountEntity.getAccountName())
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    public Optional<TransactionalAccount> toCheckingAccount(AccountEntity accountEntity) {
        return Optional.of(
                CheckingAccount.builder(
                                AccountTypes.CHECKING,
                                accountEntity.getAccountNoInt(),
                                ExactCurrencyAmount.of(
                                        accountEntity.getBalance(), accountEntity.getCurrency()))
                        .setExactAvailableCredit(calculateAvailableCredit(accountEntity))
                        .setAccountNumber(accountEntity.getAccountNoExt())
                        .setName(accountEntity.getAccountName())
                        .setBankIdentifier(accountEntity.getAccountNoInt())
                        // checking accounts are having by default the following 4 capabilities
                        // but you can confirm that easily the same way as done for other account
                        // types
                        .canExecuteExternalTransfer(AccountCapabilities.Answer.YES)
                        .canReceiveExternalTransfer(AccountCapabilities.Answer.YES)
                        .canPlaceFunds(AccountCapabilities.Answer.YES)
                        .canWithdrawCash(AccountCapabilities.Answer.YES)
                        .addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                        .sourceInfo(createAccountSourceInfo(accountEntity))
                        .build());
    }

    protected Optional<TransactionalAccount> toSavingsAccount(
            DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return Optional.of(
                SavingsAccount.builder(
                                AccountTypes.SAVINGS,
                                accountEntity.getAccountNoInt(),
                                ExactCurrencyAmount.of(
                                        accountEntity.getBalance(), accountEntity.getCurrency()))
                        .setAccountNumber(accountEntity.getAccountNoExt())
                        .setName(accountEntity.getAccountName())
                        .setBankIdentifier(accountEntity.getAccountNoInt())
                        .canExecuteExternalTransfer(
                                configuration.canExecuteExternalTransfer(
                                        accountEntity.getAccountProduct()))
                        .canReceiveExternalTransfer(
                                configuration.canReceiveExternalTransfer(
                                        accountEntity.getAccountProduct()))
                        .canPlaceFunds(
                                configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                        .canWithdrawCash(
                                configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                        .sourceInfo(createAccountSourceInfo(accountEntity))
                        .build());
    }

    protected AccountSourceInfo createAccountSourceInfo(AccountEntity accountEntity) {
        return AccountSourceInfo.builder()
                .bankProductCode(accountEntity.getAccountProduct())
                .bankAccountType(accountEntity.getAccountType())
                .build();
    }

    protected ExactCurrencyAmount calculateAvailableCredit(AccountEntity accountEntity) {
        return ExactCurrencyAmount.of(
                Math.max(accountEntity.getBalanceAvailable() - accountEntity.getBalance(), 0.0),
                accountEntity.getCurrency());
    }
}
