package se.tink.backend.common.workers.activity.generators.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.backend.core.Account;
import se.tink.backend.core.KVPair;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountBalanceActivityData {
    private Account account;
    private List<KVPair<String, Double>> data;

    public Account getAccount() {
        return account;
    }

    public List<KVPair<String, Double>> getData() {
        return data;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setData(List<KVPair<String, Double>> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("accountId", account.getId()).add("data", data).toString();
    }
}
