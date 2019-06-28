package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItem;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    @JsonProperty("accounts")
    private List<AccountsItem> accounts;

    public List<AccountsItem> getAccounts() {
        return accounts;
    }
}
