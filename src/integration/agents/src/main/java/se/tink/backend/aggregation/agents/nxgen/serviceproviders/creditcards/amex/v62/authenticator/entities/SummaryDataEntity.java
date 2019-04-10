package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SummaryDataEntity {
    private int status;
    private List<CardEntity> cardList;
    private UserDataEntity userData;

    public int getStatus() {
        return status;
    }

    public List<CardEntity> getCardList() {
        return cardList;
    }

    public UserDataEntity getUserData() {
        return userData;
    }
}
