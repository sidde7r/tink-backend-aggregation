package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {

    private String cardid;
    private String activeStatus;

    public String getCardid() {
        return cardid;
    }

    @JsonIgnore
    public boolean isActive() {
        return DnbConstants.CardStatus.ACTIVE.equalsIgnoreCase(activeStatus);
    }
}
