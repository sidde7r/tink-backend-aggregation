package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardEntity {
    private List<ActionLinksEntity> actionLinks;
    private String cardNumberMasked;
    private String expireDate;
    private int id;
    private boolean isActive;
    private String issueDate;

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }
}

@JsonObject
class ActionLinksEntity {
    private String svgIconSrc;
    private String title;
    private String url;
}
