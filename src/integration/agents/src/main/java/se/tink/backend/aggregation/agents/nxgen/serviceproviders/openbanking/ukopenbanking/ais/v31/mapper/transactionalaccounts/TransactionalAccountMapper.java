package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts;

import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.GranularBalancesMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
@Slf4j
public class TransactionalAccountMapper implements AccountMapper<TransactionalAccount> {
    private final TransactionalAccountBalanceMapper balanceMapper;
    private final IdentifierMapper identifierMapper;

    @Override
    public boolean supportsAccountType(AccountTypes type) {
        return AccountTypes.CHECKING.equals(type) || AccountTypes.SAVINGS.equals(type);
    }

    @Override
    public Optional<TransactionalAccount> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<PartyV31Entity> parties) {
        List<AccountIdentifierEntity> accountIdentifiers = account.getIdentifiers();

        AccountIdentifierEntity primaryIdentifier =
                identifierMapper.getTransactionalAccountPrimaryIdentifier(accountIdentifiers);
        String accountNumber = primaryIdentifier.getIdentification();

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(mapTransactionalAccountType(account))
                        .withInferredAccountFlags()
                        .withBalance(buildBalanceModule(balances))
                        .withId(
                                buildIdModule(
                                        account,
                                        accountNumber,
                                        primaryIdentifier,
                                        accountIdentifiers))
                        .setApiIdentifier(account.getAccountId())
                        .setHolderType(mapAccountHolderType(account));

        collectHolders(primaryIdentifier, parties).forEach(builder::addHolderName);

        return builder.build();
    }

    private IdModule buildIdModule(
            AccountEntity account,
            String accountNumber,
            AccountIdentifierEntity primaryIdentifier,
            List<AccountIdentifierEntity> accountIdentifiers) {
        IdBuildStep builder =
                IdModule.builder()
                        .withUniqueIdentifier(
                                identifierMapper.getUniqueIdentifier(primaryIdentifier))
                        .withAccountNumber(accountNumber)
                        .withAccountName(pickDisplayName(account, primaryIdentifier))
                        .addIdentifiers(identifierMapper.mapIdentifiers(accountIdentifiers));

        identifierMapper
                .getMarketSpecificIdentifier(accountIdentifiers)
                .ifPresent(builder::addIdentifier);

        return builder.build();
    }

    private BalanceModule buildBalanceModule(Collection<AccountBalanceEntity> balances) {
        BalanceBuilderStep builder =
                BalanceModule.builder()
                        .withBalanceAndGranularBalances(
                                balanceMapper.getAccountBalance(balances),
                                GranularBalancesMapper.toGranularBalances(balances));

        balanceMapper.calculateAvailableCredit(balances).ifPresent(builder::setAvailableCredit);
        balanceMapper.calculateCreditLimit(balances).ifPresent(builder::setCreditLimit);
        balanceMapper.getAvailableBalance(balances).ifPresent(builder::setAvailableBalance);

        return builder.build();
    }

    private String pickDisplayName(AccountEntity account, AccountIdentifierEntity identifier) {
        return ObjectUtils.firstNonNull(
                account.getNickname(), identifier.getOwnerName(), identifier.getIdentification());
    }

    private Collection<String> collectHolders(
            AccountIdentifierEntity primaryIdentifier, Collection<PartyV31Entity> parties) {
        return Stream.ofAll(parties)
                .map(PartyV31Entity::getName)
                .append(primaryIdentifier.getOwnerName())
                .filter(Objects::nonNull)
                .distinct()
                .toJavaList();
    }
}
