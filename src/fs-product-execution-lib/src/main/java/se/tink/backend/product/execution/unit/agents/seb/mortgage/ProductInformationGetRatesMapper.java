package se.tink.backend.product.execution.unit.agents.seb.mortgage;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.product.execution.model.FetchProductInformationParameterKey;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateResponse;

public interface ProductInformationGetRatesMapper {
    GetRateRequest toRateRequest(Map<FetchProductInformationParameterKey, Object> parameters);
    HashMap<ProductPropertyKey, Object> toProductProperties(GetRateResponse rateResponse);
}
