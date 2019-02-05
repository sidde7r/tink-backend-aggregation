package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrenciesItem;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCurrenciesResponse extends BaseResponse {

    @JsonProperty("currencies")
    private List<CurrenciesItem> currencies;

    @JsonIgnore
    public Optional<List<CurrenciesItem>> getCurrencies() {
        return Optional.ofNullable(currencies);
    }
}
