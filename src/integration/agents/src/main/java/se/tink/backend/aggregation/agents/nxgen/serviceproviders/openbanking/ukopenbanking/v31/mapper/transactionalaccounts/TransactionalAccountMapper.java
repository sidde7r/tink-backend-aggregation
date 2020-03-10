package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

@RequiredArgsConstructor
public class TransactionalAccountMapper {

    private final TransactionalAccountBalanceMapper balanceMapper;
    private final IdentifierMapper identifierMapper;

    private static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CurrentAccount")
                    .put(TransactionalAccountType.SAVINGS, "Savings")
                    .build();

    public Optional<TransactionalAccount> map(
            AccountEntity account, Collection<AccountBalanceEntity> balances, String partyName) {

        AccountIdentifierEntity primaryIdentifier =
                identifierMapper.getTransactionalAccountPrimaryIdentifier(account.getIdentifiers());
        String accountNumber = primaryIdentifier.getIdentification();
        String uniqueIdentifier =
                isRevolutAccount(account)
                        ? account.getAccountId()
                        : accountNumber; // todo get rid of revolut specific logic

        return TransactionalAccount.nxBuilder()
                .withType(
                        ACCOUNT_TYPE_MAPPER
                                .translate(account.getRawAccountSubType())
                                .orElseThrow(IllegalArgumentException::new))
                .withInferredAccountFlags()
                .withBalance(buildBalanceModule(balances))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(uniqueIdentifier)
                                .withAccountNumber(accountNumber)
                                .withAccountName(pickDisplayName(account, primaryIdentifier))
                                .addIdentifiers(
                                        Collections2.transform(
                                                account.getIdentifiers(),
                                                identifierMapper::mapIdentifier))
                                .build())
                .setApiIdentifier(account.getAccountId())
                .addHolderName(
                        ObjectUtils.firstNonNull(primaryIdentifier.getOwnerName(), partyName))
                .build();
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
}
