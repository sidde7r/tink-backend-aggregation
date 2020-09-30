package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards;

import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;

@RequiredArgsConstructor
public class CreditCardAccountMapper implements AccountMapper<CreditCardAccount> {

    protected final CreditCardBalanceMapper balanceMapper;
    private final IdentifierMapper identifierMapper;

    @Override
    public boolean supportsAccountType(AccountTypes type) {
        return AccountTypes.CREDIT_CARD.equals(type);
    }

    @Override
    public Optional<CreditCardAccount> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<IdentityDataV31Entity> parties) {

        AccountIdentifierEntity cardIdentifier =
                identifierMapper.getCreditCardIdentifier(account.getIdentifiers());
        String displayName = pickDisplayName(account, cardIdentifier);

        CreditCardBuildStep builder =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(cardIdentifier.getIdentification())
                                        .withBalance(balanceMapper.getAccountBalance(balances))
                                        .withAvailableCredit(
                                                balanceMapper.getAvailableCredit(balances))
                                        .withCardAlias(displayName)
                                        .build())
                        .withInferredAccountFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(cardIdentifier.getIdentification())
                                        .withAccountNumber(cardIdentifier.getIdentification())
                                        .withAccountName(displayName)
                                        .addIdentifier(
                                                identifierMapper.mapIdentifier(cardIdentifier))
                                        .build())
                        .setApiIdentifier(account.getAccountId());

        collectHolders(cardIdentifier, parties).forEach(builder::addHolderName);
        return Optional.of(builder.build());
    }

    protected String pickDisplayName(
            AccountEntity account, AccountIdentifierEntity cardPrimaryAccountNumber) {
        return ObjectUtils.firstNonNull(
                account.getNickname(),
                cardPrimaryAccountNumber.getOwnerName(),
                cardPrimaryAccountNumber.getIdentification());
    }

    protected Collection<String> collectHolders(
            AccountIdentifierEntity primaryIdentifier, Collection<IdentityDataV31Entity> parties) {
        return Stream.ofAll(parties)
                .map(IdentityDataV31Entity::getName)
                .append(primaryIdentifier.getOwnerName())
                .filter(Objects::nonNull)
                .distinct()
                .toJavaList();
    }
}
