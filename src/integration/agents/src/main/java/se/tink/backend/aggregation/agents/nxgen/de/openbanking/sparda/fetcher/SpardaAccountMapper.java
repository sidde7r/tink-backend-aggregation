package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.AccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@Slf4j
public class SpardaAccountMapper implements AccountMapper {
    private static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CACC", "CASH")
                    .put(TransactionalAccountType.SAVINGS, "LLSV", "ONDP", "SVGS")
                    .build();

    public Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        List<BalanceEntity> balances = accountEntity.getBalances();
        if (balances == null || balances.isEmpty()) {
            log.warn("Cannot parse account without balances! Skipping.");
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(
                        ACCOUNT_TYPE_MAPPER
                                .translate(accountEntity.getCashAccountType())
                                .orElse(null))
                .withFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                .withBalance(getBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountEntity.getIban())
                                .withAccountNumber(accountEntity.getIban())
                                .withAccountName(accountEntity.getName())
                                .addIdentifier(new IbanIdentifier(accountEntity.getIban()))
                                .build())
                .addHolderName(accountEntity.getOwnerName())
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
