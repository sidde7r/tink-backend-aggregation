package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountsEntity {
    @JsonProperty("Lista")
    private List<CardListEntity> cardList;

    @JsonProperty("ContaCartaoSelected")
    private String cardAccountSelected;

    @JsonProperty("CartaoSelected")
    private String cardSelected;

    public List<CardListEntity> getCardList() {
        return cardList;
    }

    public String getCardAccountSelected() {
        return cardAccountSelected;
    }

    public String getCardSelected() {
        return cardSelected;
    }
}
