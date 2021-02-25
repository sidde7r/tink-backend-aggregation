package se.tink.backend.aggregation.client.provider_configuration.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class FinancialService {
    private FinancialServiceSegment segment;
    private String shortName;

    public enum FinancialServiceSegment {
        BUSINESS,
        PERSONAL;
    }
}
