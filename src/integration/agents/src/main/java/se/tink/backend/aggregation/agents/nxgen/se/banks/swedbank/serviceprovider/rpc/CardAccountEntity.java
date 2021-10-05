package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardAccountEntity {
    private boolean blocked;
    private boolean internetPurchases;
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
}
