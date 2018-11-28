package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.rpc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.CardContractEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsResponse {
    private List<CardAccountEntity> cardAccounts;
    private List<CardContractEntity> cardContracts;
    private UserEntity user;

    public List<CardContractEntity> getCardContracts() {
        return Optional.ofNullable(cardContracts).orElse(Collections.emptyList());
    }

    public List<CardAccountEntity> getCardAccounts() {
        return Optional.ofNullable(cardAccounts).orElse(Collections.emptyList());
    }

    public Map<String, CardAccountEntity> getCardAccountsHashMap() {
        Map<String, CardAccountEntity> cardAccountsHashMap =
                getCardAccounts()
                        .stream()
                        .collect(Collectors.toMap(CardAccountEntity::getId, account -> account));

        return Optional.of(cardAccountsHashMap).orElse(new HashMap<>());
    }

    public UserEntity getUser() {
        return user;
    }
}
