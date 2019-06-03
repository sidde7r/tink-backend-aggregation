package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.entities.FakeAccount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DemoFinancialInstitutionAccountsResponse {
    @JsonProperty private String status;
    @JsonProperty private String message;
    @JsonProperty private List<FakeAccount> accounts;

    public DemoFinancialInstitutionAccountsResponse() {}

    public List<FakeAccount> getAccounts() {
        return Objects.nonNull(accounts) ? accounts : Collections.emptyList();
    }

    public void setAccounts(List<FakeAccount> accounts) {
        this.accounts = accounts;
    }
}
