package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrenciesItem;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCurrenciesResponse extends BaseResponse {

	@JsonProperty("currencies")
	private List<CurrenciesItem> currencies;

	public List<CurrenciesItem> getCurrencies(){
		return currencies;
	}
}