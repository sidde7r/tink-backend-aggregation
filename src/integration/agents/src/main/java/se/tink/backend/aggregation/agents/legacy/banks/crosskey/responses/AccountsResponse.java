package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.agents.rpc.Account;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountsResponse extends BaseResponse {
    public List<AccountResponse> accounts;

    public List<Account> toTinkAccounts(CrossKeyConfig config) {
        List<Account> tinkAccounts = Lists.newArrayList();

        for (AccountResponse account : getAccounts()) {

            tinkAccounts.add(account.toTinkAccount(config));
        }

        return tinkAccounts;
    }

    public List<AccountResponse> getAccounts() {
        return accounts != null ? accounts : Lists.<AccountResponse>newArrayList();
    }
}
