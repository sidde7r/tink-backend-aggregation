package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class DefaultPartnerAccountMapper implements NordeaPartnerAccountMapper {
    @Override
    public Optional<TransactionalAccount> toTinkTransactionalAccount(AccountEntity account) {
        if (Strings.isNullOrEmpty(account.getIban())
                || Strings.isNullOrEmpty(account.getCategory())
                || Strings.isNullOrEmpty(account.getCurrency())) {
            log.warn("Ignoring account with no IBAN, category and/or currency");
            return Optional.empty();
        }

        final List<AccountIdentifier> identifiers = getAccountIdentifiers(account);
        Preconditions.checkState(
                identifiers.size() != 0, "Account must have at least one identifier");
        final String formattedAccountNumber =
                MoreObjects.firstNonNull(
                        Strings.emptyToNull(account.getDisplayAccountNumber()),
                        identifiers.get(0).getIdentifier());
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        NordeaPartnerConstants.TRANSACTIONAL_ACCOUNT_TYPE_MAPPER,
                        account.getCategory())
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        MoreObjects.firstNonNull(
                                                account.getAvailableBalance(), BigDecimal.ZERO),
                                        account.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(account.getIban())
                                .withAccountNumber(identifiers.get(0).getIdentifier())
                                .withAccountName(
                                        MoreObjects.firstNonNull(
                                                Strings.emptyToNull(account.getNickname()),
                                                formattedAccountNumber))
                                .addIdentifiers(identifiers)
                                .build())
                .addHolderName(account.getHolderName())
                .setApiIdentifier(account.getAccountId())
                .build();
    }

    @JsonIgnore
    protected List<AccountIdentifier> getAccountIdentifiers(AccountEntity account) {
        return Collections.singletonList(
                AccountIdentifier.create(AccountIdentifierType.IBAN, account.getIban()));
    }
}
