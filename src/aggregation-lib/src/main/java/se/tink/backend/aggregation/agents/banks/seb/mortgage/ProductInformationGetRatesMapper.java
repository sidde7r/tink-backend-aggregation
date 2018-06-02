package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateResponse;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.core.product.ProductPropertyKey;

public interface ProductInformationGetRatesMapper {
    GetRateRequest toRateRequest(Map<FetchProductInformationParameterKey, Object> parameters);
    HashMap<ProductPropertyKey, Object> toProductProperties(GetRateResponse rateResponse);
}
