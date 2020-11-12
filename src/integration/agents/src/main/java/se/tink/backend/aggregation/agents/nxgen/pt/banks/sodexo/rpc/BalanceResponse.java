package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class BalanceResponse {

    private double balance;
    private String stamp;
    private String realtime;
    private String usage;

    @JsonProperty("benefit-date")
    private String benefitDate;
}
