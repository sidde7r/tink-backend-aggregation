package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class CardContractEntity {
    private String id;
    private String cardAccountId;
    private boolean owned;
    private String cardEngagementId;
    private String currencyCode;
    private double nonBilledAmount;
    private String productName;
    private String marketSegment;
    private String serviceLevelDescription;
    private int serviceLevel;
    private String state;
    private List<CardEntity> cards;
    private String productType;

    public String getId() {
        return id;
    }

    public String getCardAccountId() {
        return cardAccountId;
    }

    public List<CardEntity> getCards() {
        return Optional.ofNullable(cards).orElseGet(Collections::emptyList);
    }

    public boolean isOwned() {
        return owned;
    }

    public String getCardEngagementId() {
        return cardEngagementId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public double getNonBilledAmount() {
        return nonBilledAmount;
    }

    public String getProductName() {
        return productName;
    }

    public String getMarketSegment() {
        return marketSegment;
    }

    public String getServiceLevelDescription() {
        return serviceLevelDescription;
    }

    public int getServiceLevel() {
        return serviceLevel;
    }

    public String getState() {
        return state;
    }

    public String getProductType() {
        return productType;
    }

    public List<CreditCardAccount> toTinkCreditCardAccounts(
            Map<String, CardAccountEntity> accountsHashMap) {
        return cards.stream()
                .map(card -> card.toTinkCreditCardAccount(accountsHashMap, this))
                .collect(Collectors.toList());
    }
}
