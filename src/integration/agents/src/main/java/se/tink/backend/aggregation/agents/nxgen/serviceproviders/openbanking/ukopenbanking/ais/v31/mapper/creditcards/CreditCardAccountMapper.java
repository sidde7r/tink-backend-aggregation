package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;

@Slf4j
public class CreditCardAccountMapper implements AccountMapper<CreditCardAccount> {

    protected final CreditCardBalanceMapper balanceMapper;
    private final GranularBalancesMapper granularBalancesMapper;
    private final IdentifierMapper identifierMapper;

    public CreditCardAccountMapper(
            CreditCardBalanceMapper balanceMapper, IdentifierMapper identifierMapper) {
        this.balanceMapper = balanceMapper;
        this.granularBalancesMapper = new GranularBalancesMapper(new DefaultBalancePreCalculator());
        this.identifierMapper = identifierMapper;
    }

    public CreditCardAccountMapper(
            CreditCardBalanceMapper balanceMapper,
            GranularBalancesMapper granularBalancesMapper,
            IdentifierMapper identifierMapper) {
        this.balanceMapper = balanceMapper;
        this.granularBalancesMapper = granularBalancesMapper;
        this.identifierMapper = identifierMapper;
    }

    @Override
    public boolean supportsAccountType(AccountTypes type) {
        return AccountTypes.CREDIT_CARD.equals(type);
    }

    @Override
    public Optional<CreditCardAccount> map(
            AccountEntity account,
            Collection<AccountBalanceEntity> balances,
            Collection<PartyV31Entity> parties) {
        if (balances.isEmpty()) {
            log.warn(
                    "[CreditCardAccountMapper]: Something went wrong during balance fetching and balance list is empty so it could not be mapped to a transactional account - skipping account.");
            return Optional.empty();
        }
        AccountIdentifierEntity cardIdentifier =
                identifierMapper.getCreditCardIdentifier(account.getIdentifiers());
        String displayName = pickDisplayName(account, cardIdentifier);
        log.info(
                "Credit card identifier with length {} is masked: {}",
                CreditCardIdentifierUtils.getCardIdentifierLength(
                        cardIdentifier.getIdentification()),
                CreditCardIdentifierUtils.isMaskedIdentifier(cardIdentifier.getIdentification()));

        CreditCardBuildStep builder =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(cardIdentifier.getIdentification())
                                        .withBalanceAndGranularBalances(
                                                balanceMapper.getAccountBalance(balances),
                                                granularBalancesMapper.toGranularBalances(balances))
                                        .withAvailableCredit(
                                                balanceMapper.getAvailableCredit(balances))
                                        .withCardAlias(displayName)
                                        .build())
                        .withInferredAccountFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(
                                                identifierMapper.getUniqueIdentifier(
                                                        cardIdentifier))
                                        .withAccountNumber(cardIdentifier.getIdentification())
                                        .withAccountName(displayName)
                                        .addIdentifier(
                                                identifierMapper.mapIdentifier(cardIdentifier))
                                        .build())
                        .setApiIdentifier(account.getAccountId())
                        .setHolderType(mapAccountHolderType(account));

        collectHolders(cardIdentifier, parties).forEach(builder::addHolderName);
        return Optional.of(builder.build());
    }

    private String pickDisplayName(
            AccountEntity account, AccountIdentifierEntity cardPrimaryAccountNumber) {
        return ObjectUtils.firstNonNull(
                account.getNickname(),
                cardPrimaryAccountNumber.getOwnerName(),
                cardPrimaryAccountNumber.getIdentification());
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
