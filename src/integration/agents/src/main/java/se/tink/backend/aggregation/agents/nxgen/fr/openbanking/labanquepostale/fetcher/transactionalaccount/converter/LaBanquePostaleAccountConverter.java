package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@RequiredArgsConstructor
public class LaBanquePostaleAccountConverter {

    private static final List<String> BALANCE_PREFERRED_TYPES =
            ImmutableList.of(
                    BerlinGroupConstants.Accounts.CLBD, BerlinGroupConstants.Accounts.XPCD);

    private final PrioritizedValueExtractor valueExtractor;

    public List<TransactionalAccount> toTinkAccounts(AccountResponse accountsResponse) {
        return Optional.ofNullable(accountsResponse.getAccounts()).orElse(Collections.emptyList())
                .stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        return BerlinGroupConstants.ACCOUNT_TYPE_MAPPER
                .translate(accountEntity.getCashAccountType().name())
                .flatMap(type -> toTransactionalAccount(accountEntity, type));
    }

    private Optional<TransactionalAccount> toTransactionalAccount(
            AccountEntity accountEntity, TransactionalAccountType type) {
        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance(accountEntity)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountEntity.getUniqueIdentifier())
                                .withAccountNumber(accountEntity.getAccountNumber())
                                .withAccountName(accountEntity.getName())
                                .addIdentifier(accountEntity.getIdentifier())
                                .build())
                .putInTemporaryStorage(
                        BerlinGroupConstants.StorageKeys.TRANSACTIONS_URL,
                        accountEntity.getTransactionLink())
                .setApiIdentifier(accountEntity.getResourceId())
                .setBankIdentifier(accountEntity.getUniqueIdentifier())
                .addHolderName(accountEntity.getName())
                .build();
    }

    public ExactCurrencyAmount getBalance(AccountEntity accountEntity) {
        return valueExtractor
                .pickByValuePriority(
                        accountEntity.getBalances(),
                        BalanceBaseEntity::getBalanceType,
                        BALANCE_PREFERRED_TYPES)
                .filter(balance -> doesMatchWithAccountCurrency(accountEntity, balance))
                .map(balance -> balance.getBalanceAmount().toAmount())
                .orElseThrow(
                        () ->
                                new AccountRefreshException(
                                        "Could not extract account balance. No available balance with type of: "
                                                + StringUtils.join(BALANCE_PREFERRED_TYPES, ", ")));
    }

    private static boolean doesMatchWithAccountCurrency(
            AccountEntity accountEntity, BalanceBaseEntity balance) {
        return balance.isInCurrency(accountEntity.getAccountId().getCurrency());
    }
}
