package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.Accounts;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@RequiredArgsConstructor
public class LaBanquePostaleAccountConverter {

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
                .withBalance(getBalanceModule(accountEntity.getBalances()))
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

    private BalanceModule getBalanceModule(List<BalanceBaseEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceBaseEntity> balances) {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        Optional<BalanceBaseEntity> balanceEntity =
                balances.stream()
                        .filter(b -> Accounts.CLBD.equalsIgnoreCase(b.getBalanceType()))
                        .findAny();

        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }
        return balanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(BalanceBaseEntity::toAmount)
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceBaseEntity> balances) {
        return balances.stream()
                .filter(b -> Accounts.XPCD.equalsIgnoreCase(b.getBalanceType()))
                .findAny()
                .map(BalanceBaseEntity::toAmount);
    }
}
