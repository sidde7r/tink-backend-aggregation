package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.entity.CreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreditCardsResponse {
    private List<CreditCardEntity> cards;
}
