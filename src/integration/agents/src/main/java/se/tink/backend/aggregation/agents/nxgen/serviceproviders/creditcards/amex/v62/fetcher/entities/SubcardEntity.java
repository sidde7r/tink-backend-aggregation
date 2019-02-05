package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubcardEntity {
    private String cardMemberName;
    private String cardProductName;
    private String suppIndex;

    public String getCardMemberName() {
        return cardMemberName;
    }

    public String getCardProductName() {
        return cardProductName;
    }

    public String getSuppIndex() {
        return suppIndex;
    }
}
