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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
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
            Map<String, AccountDetailsResponse> accountDetails,
            List<CardEntity> cardEntities) {

        return accounts.stream()
                .map(
                        account ->
                                toCreditCardAccount(
                                        configuration,
                                        account,
                                        accountDetails.get(account.getAccountNoExt()),
                                        getCardEntity(account, cardEntities)))
                .collect(Collectors.toList());
    }

    private CardEntity getCardEntity(AccountEntity accountEntity, List<CardEntity> cardEntities) {
        List<CardEntity> cardsOfAccount =
                cardEntities.stream()
                        .filter(cardEntity -> accountNumbersAreEqual(accountEntity, cardEntity))
                        .collect(Collectors.toList());

        if (cardsOfAccount.isEmpty()) {
            log.warn(
                    "Couldn't find a card for credit card account! Is cards list empty: {}",
                    cardEntities.isEmpty());
            return new CardEntity();
        }
        if (cardsOfAccount.size() > 1) {
            log.warn(
                    "Credit card account has more than 1 credit card. Size of cards: {}",
                    cardsOfAccount.size());
        }
        return cardsOfAccount.get(0);
    }

    private boolean accountNumbersAreEqual(AccountEntity accountEntity, CardEntity cardEntity) {
        return accountEntity.getAccountNoInt().equals(cardEntity.getAccountNumber());
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
            AccountDetailsResponse accountDetailsResponse,
            CardEntity cardEntity) {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(buildCreditCardModule(accountEntity, cardEntity))
                .withoutFlags()
                .withId(
                        buildIdModule(
                                accountEntity,
                                getCreditCardAccountIdentifiers(
                                        accountEntity, accountDetailsResponse, cardEntity)))
                .setBankIdentifier(accountEntity.getAccountNoInt())
                .setApiIdentifier(accountEntity.getAccountNoInt())
                .canExecuteExternalTransfer(
                        configuration.canExecuteExternalTransfer(accountEntity.getAccountProduct()))
                .canReceiveExternalTransfer(
                        configuration.canReceiveExternalTransfer(accountEntity.getAccountProduct()))
                .canPlaceFunds(configuration.canPlaceFunds(accountEntity.getAccountProduct()))
                .canWithdrawCash(configuration.canWithdrawCash(accountEntity.getAccountProduct()))
                .sourceInfo(createAccountSourceInfo(accountEntity))
                .addParties(getAccountParties(accountDetailsResponse.getAccountOwners(marketCode)))
                .build();
    }

    private CreditCardModule buildCreditCardModule(
            AccountEntity accountEntity, CardEntity cardEntity) {
        return CreditCardModule.builder()
                .withCardNumber(
                        StringUtils.isNotBlank(cardEntity.getMaskedCardNumber())
                                ? cardEntity.getMaskedCardNumber()
                                : accountEntity.getAccountNoExt())
                .withBalance(
                        ExactCurrencyAmount.of(
                                accountEntity.getBalance(), accountEntity.getCurrency()))
                .withAvailableCredit(calculateAvailableCredit(accountEntity))
                .withCardAlias(
                        StringUtils.isNotBlank(cardEntity.getCardType())
                                ? cardEntity.getCardType()
                                : accountEntity.getAccountName())
                .build();
    }

    private List<AccountIdentifier> getCreditCardAccountIdentifiers(
            AccountEntity accountEntity,
            AccountDetailsResponse accountDetailsResponse,
            CardEntity cardEntity) {
        List<AccountIdentifier> identifiers = getIdentifiers(accountEntity, accountDetailsResponse);
        if (StringUtils.isNotBlank(cardEntity.getMaskedCardNumber())) {
            identifiers.add(new MaskedPanIdentifier(cardEntity.getMaskedCardNumber()));
        }
        return identifiers;
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
                .withId(
                        buildIdModule(
                                accountEntity,
                                getIdentifiers(accountEntity, accountDetailsResponse)))
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
                .withId(
                        buildIdModule(
                                accountEntity,
                                getIdentifiers(accountEntity, accountDetailsResponse)))
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
            AccountEntity accountEntity, List<AccountIdentifier> identifiers) {
        return IdModule.builder()
                .withUniqueIdentifier(getUniqueIdentifier(accountEntity))
                .withAccountNumber(accountEntity.getAccountNoExt())
                .withAccountName(accountEntity.getAccountName())
                .addIdentifiers(identifiers)
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
            identifiers.add(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
        }
        return identifiers;
    }

    protected AccountIdentifierType getAccountIdentifierType(String marketCode) {
        return Optional.ofNullable(AccountIdentifierType.fromScheme(marketCode.toLowerCase()))
                .orElse(AccountIdentifierType.COUNTRY_SPECIFIC);
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
                        .withId(
                                buildIdModule(
                                        accountEntity,
                                        getIdentifiers(accountEntity, accountDetailsResponse)))
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
