package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class AccountEntityMapper {

    private final String marketCode;

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
            DanskeBankConfiguration configuration,
            AccountEntity accountEntity,
            AccountDetailsResponse accountDetailsResponse) {
        return LoanAccount.builder(
                        accountEntity.getAccountNoInt(),
                        ExactCurrencyAmount.of(
                                accountEntity.getBalance(), accountEntity.getCurrency()))
                .setAccountNumber(accountEntity.getAccountNoExt())
                .setName(accountEntity.getAccountName())
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setInterestRate(accountDetailsResponse.getInterestRate())
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .setDetails(
                        LoanDetails.builder(getLoanType(configuration, accountEntity))
                                .setLoanNumber(accountEntity.getAccountNoExt())
                                .setApplicants(accountDetailsResponse.getAccountOwners(marketCode))
                                .build())
                .build();
    }

    private LoanDetails.Type getLoanType(
            final DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return configuration
                .getLoanAccountTypes()
                .getOrDefault(accountEntity.getAccountProduct(), LoanDetails.Type.DERIVE_FROM_NAME);
    }

    public CreditCardAccount toCreditCardAccount(
            DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return CreditCardAccount.builder(
                        getUniqueIdentifier(accountEntity),
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

    protected String getUniqueIdentifier(AccountEntity accountEntity) {
        return accountEntity.getAccountNoInt();
    }

    public Optional<TransactionalAccount> toUnknownAccount(AccountEntity accountEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                accountEntity.getBalance(),
                                                accountEntity.getCurrency()))
                                .setAvailableCredit(calculateAvailableCredit(accountEntity))
                                .build())
                .withId(buildIdModule(accountEntity))
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setApiIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    public Optional<TransactionalAccount> toCheckingAccount(AccountEntity accountEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                accountEntity.getBalance(),
                                                accountEntity.getCurrency()))
                                .setAvailableCredit(calculateAvailableCredit(accountEntity))
                                .build())
                .withId(buildIdModule(accountEntity))
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setApiIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(AccountCapabilities.Answer.YES)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.YES)
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    private IdModule buildIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(getUniqueIdentifier(accountEntity))
                .withAccountNumber(accountEntity.getAccountNoExt())
                .withAccountName(accountEntity.getAccountName())
                .addIdentifier(
                        AccountIdentifier.create(
                                getAccountIdentifierType(marketCode),
                                accountEntity.getAccountNoExt()))
                .build();
    }

    protected AccountIdentifier.Type getAccountIdentifierType(String marketCode) {
        return Optional.ofNullable(AccountIdentifier.Type.fromScheme(marketCode.toLowerCase()))
                .orElse(AccountIdentifier.Type.COUNTRY_SPECIFIC);
    }

    public Optional<TransactionalAccount> toSavingsAccount(
            DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        accountEntity.getBalance(), accountEntity.getCurrency())))
                .withId(buildIdModule(accountEntity))
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setApiIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    private AccountSourceInfo createAccountSourceInfo(AccountEntity accountEntity) {
        return AccountSourceInfo.builder()
                .bankProductCode(accountEntity.getAccountProduct())
                .bankAccountType(accountEntity.getAccountType())
                .build();
    }

    private ExactCurrencyAmount calculateAvailableCredit(AccountEntity accountEntity) {
        return ExactCurrencyAmount.of(
                Math.max(accountEntity.getBalanceAvailable() - accountEntity.getBalance(), 0.0),
                accountEntity.getCurrency());
    }
}
