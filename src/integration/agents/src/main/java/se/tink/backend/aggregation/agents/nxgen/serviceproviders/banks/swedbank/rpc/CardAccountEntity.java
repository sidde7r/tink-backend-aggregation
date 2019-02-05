package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountEntity {
    private boolean visaBusinessCard;
    private LinksEntity links;
    private String name;
    private String id;
    private String currency;
    private DetailsEntity details;
    private String availableAmount;
    private boolean availableForFavouriteAccount;
    private boolean availableForPriorityAccount;
    private String cardNumber;

    public boolean isVisaBusinessCard() {
        return visaBusinessCard;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public DetailsEntity getDetails() {
        return details;
    }

    public String getAvailableAmount() {
        return availableAmount;
    }

    public boolean isAvailableForFavouriteAccount() {
        return availableForFavouriteAccount;
    }

    public boolean isAvailableForPriorityAccount() {
        return availableForPriorityAccount;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}
