package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DemoFinancialInstitutionAccountBody {
    @JsonProperty private String username;
    @JsonProperty private String token;

    public DemoFinancialInstitutionAccountBody(String username, String token) {
        this.username = username;
        this.token = token;
    }
}
