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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

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
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }
}
