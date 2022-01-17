package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.entity.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.NordeaPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountListResponse {
    private List<AccountEntity> result;
    private List<AccountEntity> accounts;
    private List<CardEntity> cards;

    public Collection<TransactionalAccount> toTinkTransactionalAccounts(
            NordeaPartnerAccountMapper accountMapper, boolean isOnStaging) {
        return Stream.of(result, accounts).filter(Objects::nonNull).findFirst()
                .orElse(Collections.emptyList()).stream()
                .filter(AccountEntity::hasIban)
                .map(
                        accountEntity ->
                                accountMapper.toTinkTransactionalAccount(
                                        accountEntity, isOnStaging))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Collection<CreditCardAccount> toTinkCreditCardAccounts() {
        return Optional.ofNullable(cards).orElse(Collections.emptyList()).stream()
                .map(CardEntity::toTinkCreditCardAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
