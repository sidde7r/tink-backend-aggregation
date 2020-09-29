package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.CreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreditCardListResponse {
    @JsonProperty("list")
    private List<CreditCardEntity> creditCards;
}
