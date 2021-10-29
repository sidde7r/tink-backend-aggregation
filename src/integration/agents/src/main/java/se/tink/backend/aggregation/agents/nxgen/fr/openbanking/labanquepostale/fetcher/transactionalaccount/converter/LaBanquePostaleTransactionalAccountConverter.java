package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
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
public class LaBanquePostaleTransactionalAccountConverter {

    public Optional<TransactionalAccount> toTransactionalAccount(AccountEntity accountEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule(accountEntity.getBalances()))
                .withId(getIdModule(accountEntity))
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

    private IdModule getIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getUniqueIdentifier())
                .withAccountNumber(accountEntity.getAccountNumber())
                .withAccountName(accountEntity.getName())
                .addIdentifier(accountEntity.getIdentifier())
                .build();
    }
}
