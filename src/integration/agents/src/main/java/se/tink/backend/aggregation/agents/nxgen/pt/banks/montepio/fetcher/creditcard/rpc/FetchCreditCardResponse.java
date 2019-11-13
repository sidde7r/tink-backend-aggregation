package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.entity.CreditCardsResultEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCreditCardResponse extends GenericResponse {

    @JsonProperty("Result")
    private CreditCardsResultEntity result;

    public CreditCardsResultEntity getResult() {
        return result;
    }
}
