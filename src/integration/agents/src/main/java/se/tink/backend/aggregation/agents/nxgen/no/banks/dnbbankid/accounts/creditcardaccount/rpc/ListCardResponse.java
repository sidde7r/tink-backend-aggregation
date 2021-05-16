package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ListCardResponse {
    private List<CardEntity> debetCards;
    private List<CardEntity> creditCards;
}
