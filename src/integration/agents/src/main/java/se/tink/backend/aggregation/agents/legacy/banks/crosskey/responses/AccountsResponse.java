package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountsResponse extends BaseResponse {
    public List<AccountResponse> accounts;

    public List<AccountResponse> getAccounts() {
        return accounts != null ? accounts : Lists.<AccountResponse>newArrayList();
    }
}
