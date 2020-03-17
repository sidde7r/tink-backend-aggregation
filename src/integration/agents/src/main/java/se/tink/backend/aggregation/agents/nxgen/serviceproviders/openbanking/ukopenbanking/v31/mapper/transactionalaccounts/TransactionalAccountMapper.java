package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import com.google.common.collect.Collections2;
import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

@RequiredArgsConstructor
public class TransactionalAccountMapper {

    private final TransactionalAccountBalanceMapper balanceMapper;
    private final IdentifierMapper identifierMapper;

    public Optional<TransactionalAccount> map(
            AccountEntity account,
            TransactionalAccountType accountType,
            Collection<AccountBalanceEntity> balances,
            Collection<IdentityDataV31Entity> parties) {

        AccountIdentifierEntity primaryIdentifier =
                identifierMapper.getTransactionalAccountPrimaryIdentifier(account.getIdentifiers());
        String accountNumber = primaryIdentifier.getIdentification();
        String uniqueIdentifier =
                isRevolutAccount(account)
                        ? account.getAccountId()
                        : accountNumber; // todo get rid of revolut specific logic

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withType(accountType)
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
                                                        account.getIdentifiers(),
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
                .distinct()
                .toJavaList();
    }
}
