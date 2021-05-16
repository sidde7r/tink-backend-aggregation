package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.checkingaccount.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.checkingaccount.entities.AccountDetailsEntity;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListResponse {
    private List<AccountDetailsEntity> accountList;

    public List<AccountDetailsEntity> getAccountList() {
        return accountList;
    }
}
