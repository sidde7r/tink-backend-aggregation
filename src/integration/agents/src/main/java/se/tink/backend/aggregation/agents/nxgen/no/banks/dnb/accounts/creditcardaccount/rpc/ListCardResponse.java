package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ListCardResponse {
    private List<CardEntity> debetCards = Collections.emptyList();
    private List<CardEntity> creditCards = Collections.emptyList();
}
