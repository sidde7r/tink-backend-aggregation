package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class AccountMapper {

    public Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .withBalance(getBalanceModule(accountEntity.getBalances()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountEntity.getIban())
                                .withAccountNumber(accountEntity.getIban())
                                .withAccountName(accountEntity.getIban())
                                .addIdentifier(new IbanIdentifier(accountEntity.getIban()))
                                .build())
                .setApiIdentifier(accountEntity.getResourceId())
                .build();
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(BalanceMapper.getBookedBalance(balances));
        BalanceMapper.getAvailableBalance(balances)
                .ifPresent(balanceBuilderStep::setAvailableBalance);
        BalanceMapper.getCreditLimit(balances).ifPresent(balanceBuilderStep::setCreditLimit);
        return balanceBuilderStep.build();
    }
}
