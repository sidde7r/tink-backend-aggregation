package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.executor.beneficiary.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Balance {
    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("value")
    private Object value;
}
