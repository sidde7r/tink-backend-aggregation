package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class FinancialService {
    private FinancialServiceSegment segment;
    private String shortName;

    public enum FinancialServiceSegment {
        BUSINESS,
        PERSONAL;
    }

    @JsonIgnore
    public static FinancialService of(
            se.tink.backend.aggregation.client.provider_configuration.rpc.FinancialService
                    financialService) {
        if (financialService == null || financialService.getSegment() == null) {
            return null;
        }
        return new FinancialService()
                .setSegment(FinancialServiceSegment.valueOf(financialService.getSegment().name()))
                .setShortName(financialService.getShortName());
    }
}
