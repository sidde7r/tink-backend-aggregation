package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCardBalancesOutEntity {

    @JsonIgnore
    private static <T> Predicate<T> distinctByProperties(Function<? super T, ?> propertyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(propertyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private List<CardsEntity> cards;

    /**
     * Returns a list of distinct cards returned by the bank. If the id, owner name and balance are equal for two cards
     * the are considered to be the same card, and only one cardsEntity will be returned as a list.
     */
    @JsonIgnore
    public List<CardsEntity> getDistinctCardsList() {
        return cards
                .stream()
                .filter(distinctByProperties(cardsEntity -> Arrays
                        .asList(cardsEntity.getCardId(), cardsEntity.getOwnerName(), cardsEntity.getBalance())))
                .collect(Collectors.toList());
    }

    public List<CardsEntity> getCards() {
        return cards;
    }

    public void setCards(List<CardsEntity> cards) {
        this.cards = cards;
    }
}
