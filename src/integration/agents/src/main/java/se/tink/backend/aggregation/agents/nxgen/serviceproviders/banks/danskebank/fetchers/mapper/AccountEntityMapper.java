package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public class AccountEntityMapper {

    private final String marketCode;

    public List<TransactionalAccount> toTinkCheckingAccounts(
            List<String> knownCheckingAccountProducts,
            List<AccountEntity> accounts,
            Map<String, AccountDetailsResponse> accountDetails) {
        return accounts.stream()
                .filter(
                        DanskeBankPredicates.knownCheckingAccountProducts(
                                knownCheckingAccountProducts))
                .map(
                        account ->
                                toCheckingAccount(
                                        account, accountDetails.get(account.getAccountNoExt())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<TransactionalAccount> toTinkSavingsAccounts(
            DanskeBankConfiguration configuration,
            List<AccountEntity> accounts,
            Map<String, AccountDetailsResponse> accountDetails) {
        return accounts.stream()
                .filter(
                        DanskeBankPredicates.knownSavingsAccountProducts(
                                configuration.getSavingsAccountTypes()))
                .map(
                        account ->
                                toSavingsAccount(
                                        configuration,
                                        account,
                                        accountDetails.get(account.getAccountNoExt())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<CreditCardAccount> toTinkCreditCardAccounts(
            DanskeBankConfiguration configuration,
            List<AccountEntity> accounts,
            Map<String, AccountDetailsResponse> accountDetails) {
        return accounts.stream()
                .map(
                        account ->
                                toCreditCardAccount(
                                        configuration,
                                        account,
                                        accountDetails.get(account.getAccountNoExt())))
                .collect(Collectors.toList());
    }

    public LoanAccount toLoanAccount(
            DanskeBankConfiguration configuration,
            AccountEntity accountEntity,
            AccountDetailsResponse accountDetailsResponse) {

        List<String> accountOwners = accountDetailsResponse.getAccountOwners(marketCode);

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(getLoanType(configuration, accountEntity))
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                accountEntity.getBalance(),
                                                accountEntity.getCurrency()))
                                .withInterestRate(accountDetailsResponse.getInterestRate())
                                .setLoanNumber(accountEntity.getAccountNoExt())
                                .setApplicants(accountOwners.isEmpty() ? null : accountOwners)
                                .setCoApplicant(accountOwners.size() > 1)
                                .build())
                .withId(
                        IdModule.builder()
                                // Do not change the uniqueIdentifier to use getUniqueIdentifier()
                                // The SE account mapper overrides it to use accountNoExt, which
                                // will cause duplicates!!!!
                                .withUniqueIdentifier(accountEntity.getAccountNoInt())
                                .withAccountNumber(accountEntity.getAccountNoExt())
                                .withAccountName(accountEntity.getAccountName())
                                .addIdentifier(
                                        getLoanIdentifier(accountEntity, accountDetailsResponse))
                                .setProductName(accountDetailsResponse.getAccountType())
                                .build())
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setApiIdentifier(accountEntity.getAccountNoInt())
                .addParties(getAccountParties(accountOwners))
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    private AccountIdentifier getLoanIdentifier(
            AccountEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
        return StringUtils.isNotBlank(accountDetailsResponse.getIban())
                ? new IbanIdentifier(accountDetailsResponse.getIban())
                : AccountIdentifier.create(
                        getAccountIdentifierType(marketCode), accountEntity.getAccountNoExt());
    }

    private List<Party> getAccountParties(List<String> accountOwners) {
        return accountOwners.stream()
                .map(owner -> new Party(owner, Party.Role.HOLDER))
                .collect(Collectors.toList());
    }

    private LoanDetails.Type getLoanType(
            final DanskeBankConfiguration configuration, AccountEntity accountEntity) {
        return configuration
                .getLoanAccountTypes()
                .getOrDefault(accountEntity.getAccountProduct(), LoanDetails.Type.DERIVE_FROM_NAME);
    }

    public CreditCardAccount toCreditCardAccount(
            DanskeBankConfiguration configuration,
            AccountEntity accountEntity,
            AccountDetailsResponse accountDetailsResponse) {

        HolderName holderName =
                getAccountParties(accountDetailsResponse.getAccountOwners(marketCode)).stream()
                        .map(party -> new HolderName(party.getName()))
                        .findFirst()
                        .orElse(null);

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
                .setHolderName(holderName)
                .build();
    }

    protected String getUniqueIdentifier(AccountEntity accountEntity) {
        return accountEntity.getAccountNoInt();
    }

    public Optional<TransactionalAccount> toUnknownAccount(
            AccountEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
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
                .withId(buildIdModule(accountEntity, accountDetailsResponse))
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setApiIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .build();
    }

    public Optional<TransactionalAccount> toCheckingAccount(
            AccountEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
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
                .withId(buildIdModule(accountEntity, accountDetailsResponse))
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setApiIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(AccountCapabilities.Answer.YES)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.YES)
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                .addAccountFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .addParties(getAccountParties(accountDetailsResponse.getAccountOwners(marketCode)))
                .build();
    }

    private IdModule buildIdModule(
            AccountEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
        return IdModule.builder()
                .withUniqueIdentifier(getUniqueIdentifier(accountEntity))
                .withAccountNumber(accountEntity.getAccountNoExt())
                .withAccountName(accountEntity.getAccountName())
                .addIdentifiers(getIdentifiers(accountEntity, accountDetailsResponse))
                .build();
    }

    private List<AccountIdentifier> getIdentifiers(
            AccountEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
        String iban = accountDetailsResponse.getIban();
        List<AccountIdentifier> identifiers = new ArrayList<>();
        identifiers.add(
                AccountIdentifier.create(
                        getAccountIdentifierType(marketCode), accountEntity.getAccountNoExt()));
        if (iban != null) {
            identifiers.add(AccountIdentifier.create(Type.IBAN, iban));
        }
        return identifiers;
    }

    protected AccountIdentifier.Type getAccountIdentifierType(String marketCode) {
        return Optional.ofNullable(AccountIdentifier.Type.fromScheme(marketCode.toLowerCase()))
                .orElse(AccountIdentifier.Type.COUNTRY_SPECIFIC);
    }

    public Optional<TransactionalAccount> toSavingsAccount(
            DanskeBankConfiguration configuration,
            AccountEntity accountEntity,
            AccountDetailsResponse accountDetailsResponse) {
        TransactionalBuildStep transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withPaymentAccountFlag()
                        .withBalance(
                                BalanceModule.of(
                                        ExactCurrencyAmount.of(
                                                accountEntity.getBalance(),
                                                accountEntity.getCurrency())))
                        .withId(buildIdModule(accountEntity, accountDetailsResponse))
                        .setBankIdentifier(accountEntity.getAccountNoInt())
                        .setApiIdentifier(accountEntity.getAccountNoInt())
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
                        .addParties(
                                getAccountParties(
                                        accountDetailsResponse.getAccountOwners(marketCode)));
        if (configuration
                .getDepotCashBalanceAccounts()
                .contains(accountEntity.getAccountProduct())) {
            transactionalAccount.addAccountFlags(AccountFlag.DEPOT_CASH_BALANCE);
        }
        return transactionalAccount.build();
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
