package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import io.vavr.collection.Stream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
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
            Collection<PartyV31Entity> parties) {

        AccountIdentifierEntity cardIdentifier =
                identifierMapper.getCreditCardIdentifier(account.getIdentifiers());
        String displayName = pickDisplayName(account, cardIdentifier);
        log.info(
                "Credit card identifier with length {} is masked: {}",
                CreditCardIdentifierUtils.getCardIdentifierLength(
                        cardIdentifier.getIdentification()),
                CreditCardIdentifierUtils.isMaskedIdentifier(cardIdentifier.getIdentification()));

        Map<AccountBalanceType, ExactCurrencyAmount> granularAccountBalances = new HashMap<>();

        try {
            granularAccountBalances =
                    balances.stream()
                            .collect(
                                    Collectors.toMap(
                                            balance -> mapToAccountBalanceType(balance.getType()),
                                            AccountBalanceEntity::getAmount));
        } catch (Exception e) {
            log.warn("Could not put granular balances into the builder");
        }

        CreditCardBuildStep builder =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(cardIdentifier.getIdentification())
                                        .withGranularBalance(
                                                balanceMapper.getAccountBalance(balances),
                                                granularAccountBalances)
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

    // TODO (AAP-1566): For now AccountBalanceType is clone of
    // UkOpenBankingApiDefinitions.AccountBalanceType
    // so this mapping will work fine but in the future they might differ so an explicit mapping
    // would
    // be better
    // TODO (AAP-1566): This method is repeated in TransactionalAccountMapper in UKOB framework,
    // avoid it
    private AccountBalanceType mapToAccountBalanceType(
            UkOpenBankingApiDefinitions.AccountBalanceType type) {
        return AccountBalanceType.valueOf(type.name());
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
