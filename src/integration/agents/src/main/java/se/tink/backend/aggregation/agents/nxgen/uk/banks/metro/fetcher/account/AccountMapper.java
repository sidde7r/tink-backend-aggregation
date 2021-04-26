package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.AccountConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class AccountMapper {

    private final AccountTypeMapper accountTypeMapper;

    List<TransactionalAccount> map(List<AccountEntity> accounts, String holderName) {
        return accounts.stream()
                .map(
                        account ->
                                TransactionalAccount.nxBuilder()
                                        .withTypeAndFlagsFrom(
                                                accountTypeMapper, account.getAccountType().name())
                                        .withBalance(
                                                BalanceModule.of(createCurrencyAmount(account)))
                                        .withId(createIdModule(account))
                                        .setBankIdentifier(account.getAccountId())
                                        .addParties(createParty(holderName))
                                        .putInTemporaryStorage(
                                                AccountConstants.CREATION_DATE,
                                                account.getCreationDate().toString())
                                        .putInTemporaryStorage(
                                                AccountConstants.ACCOUNT_TYPE,
                                                account.getAccountType().name())
                                        .putInTemporaryStorage(
                                                AccountConstants.CURRENCY, account.getCurrency())
                                        .putInTemporaryStorage(
                                                AccountConstants.ACCOUNT_ID, account.getAccountId())
                                        .build())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static ExactCurrencyAmount createCurrencyAmount(AccountEntity account) {
        return ExactCurrencyAmount.of(
                account.getAvailableBalance().getAmount(),
                account.getAvailableBalance().getCurrency());
    }

    private static IdModule createIdModule(AccountEntity account) {
        return IdModule.builder()
                .withUniqueIdentifier(account.getIban())
                .withAccountNumber(account.getIban())
                .withAccountName(account.getNickname())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.IBAN,
                                account.getAccountId(),
                                account.getNickname()))
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.SORT_CODE,
                                account.getSortCode() + account.getAccountId(),
                                account.getNickname()))
                .build();
    }

    private static Party createParty(String holderName) {
        return Optional.ofNullable(holderName)
                .map(name -> new Party(name, Role.HOLDER))
                .orElse(null);
    }
}
