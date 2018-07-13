package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities.AccountEntity;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {

    @JsonProperty("tepgetaccounts")
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
