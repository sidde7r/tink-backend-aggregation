package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common;

import com.google.common.collect.Collections2;
import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.calculator.DefaultBalancePreCalculator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.GranularBalancesMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
public class RevolutTransactionalAccountMapper implements AccountMapper<TransactionalAccount> {
    private final TransactionalAccountBalanceMapper balanceMapper;
    private final GranularBalancesMapper granularBalancesMapper;
    private final IdentifierMapper identifierMapper;

    public RevolutTransactionalAccountMapper(
            TransactionalAccountBalanceMapper balanceMapper, IdentifierMapper identifierMapper) {
        this.balanceMapper = balanceMapper;
        this.granularBalancesMapper = new GranularBalancesMapper(new DefaultBalancePreCalculator());
        this.identifierMapper = identifierMapper;
    }

    @Override
    public boolean supportsAccountType(AccountTypes type) {
        return AccountTypes.CHECKING.equals(type) || AccountTypes.SAVINGS.equals(type);
    }

    @Override
    public Optional<TransactionalAccount> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<PartyV31Entity> parties) {
        if (balances.isEmpty()) {
            log.warn(
                    "[RevolutTransactionalAccountMapper]: Something went wrong during balance fetching and balance list is empty so it could not be mapped to a transactional account - skipping account.");
            return Optional.empty();
        }
        List<AccountIdentifierEntity> accountIdentifiers =
                account.getIdentifiers().stream().distinct().collect(Collectors.toList());

        AccountIdentifierEntity primaryIdentifier =
                identifierMapper.getTransactionalAccountPrimaryIdentifier(accountIdentifiers);
        String accountNumber = primaryIdentifier.getIdentification();
        // unique identifier is not IBAN, because revolut can return multiple accounts with the same
        // IBAN and different currency.
        // it is also needed for transfer destination fetching
        String uniqueIdentifier = account.getAccountId();

        return buildTransactionalAccount(
                account,
                balances,
                parties,
                accountIdentifiers,
                primaryIdentifier,
                accountNumber,
                uniqueIdentifier);
    }

    private Optional<TransactionalAccount> buildTransactionalAccount(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<PartyV31Entity> parties,
            List<AccountIdentifierEntity> accountIdentifiers,
            AccountIdentifierEntity primaryIdentifier,
            String accountNumber,
            String uniqueIdentifier) {
        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(mapTransactionalAccountType(account))
                        .withInferredAccountFlags()
                        .withBalance(buildBalanceModule(balances))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(uniqueIdentifier)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(
                                                pickDisplayName(account, primaryIdentifier))
                                        .addIdentifiers(
                                                Collections2.transform(
                                                        accountIdentifiers,
                                                        identifierMapper::mapIdentifier))
                                        .build())
                        .setApiIdentifier(account.getAccountId())
                        .setHolderType(mapAccountHolderType(account));

        collectHolders(primaryIdentifier, parties).forEach(builder::addHolderName);

        return builder.build();
    }

    private BalanceModule buildBalanceModule(Collection<AccountBalanceEntity> balances) {
        BalanceBuilderStep builder =
                BalanceModule.builder()
                        .withBalanceAndGranularBalances(
                                balanceMapper.getAccountBalance(balances),
                                granularBalancesMapper.toGranularBalances(balances));

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
