package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import com.google.common.collect.Collections2;
import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

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
            Collection<IdentityDataV31Entity> parties) {
        List<AccountIdentifierEntity> accountIdentifiers = account.getIdentifiers();

        AccountIdentifierEntity primaryIdentifier =
                identifierMapper.getTransactionalAccountPrimaryIdentifier(accountIdentifiers);
        String accountNumber = primaryIdentifier.getIdentification();
        String uniqueIdentifier =
                isRevolutAccount(account)
                        ? account.getAccountId()
                        : accountNumber; // todo get rid of revolut specific logic

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(mapType(account))
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
                        .setApiIdentifier(account.getAccountId());

        collectHolders(primaryIdentifier, parties).forEach(builder::addHolderName);

        return builder.build();
    }

    private BalanceModule buildBalanceModule(Collection<AccountBalanceEntity> balances) {
        BalanceBuilderStep builder =
                BalanceModule.builder().withBalance(balanceMapper.getAccountBalance(balances));

        balanceMapper.calculateAvailableCredit(balances).ifPresent(builder::setAvailableCredit);
        balanceMapper.calculateCreditLimit(balances).ifPresent(builder::setCreditLimit);
        balanceMapper.getAvailableBalance(balances).ifPresent(builder::setAvailableBalance);

        return builder.build();
    }

    private boolean isRevolutAccount(AccountEntity account) {
        return account.getIdentifiers().stream()
                .filter(i -> i.getIdentifierType().equals(IBAN))
                .map(AccountIdentifierEntity::getIdentification)
                .anyMatch(i -> i.contains("REVO"));
    }

    private String pickDisplayName(AccountEntity account, AccountIdentifierEntity identifier) {
        return ObjectUtils.firstNonNull(
                account.getNickname(), identifier.getOwnerName(), identifier.getIdentification());
    }

    private Collection<String> collectHolders(
            AccountIdentifierEntity primaryIdentifier, Collection<IdentityDataV31Entity> parties) {
        return Stream.ofAll(parties)
                .map(IdentityDataV31Entity::getName)
                .append(primaryIdentifier.getOwnerName())
                .filter(Objects::nonNull)
                .distinct()
                .toJavaList();
    }

    private TransactionalAccountType mapType(AccountEntity account) {
        if ("CurrentAccount".equals(account.getRawAccountSubType())) {
            return TransactionalAccountType.CHECKING;
        } else if ("Savings".equals(account.getRawAccountSubType())) {
            return TransactionalAccountType.SAVINGS;
        }
        throw new IllegalStateException(
                "Cannot map to transactional account. Wrong account type passed to the mapper");
    }
}
