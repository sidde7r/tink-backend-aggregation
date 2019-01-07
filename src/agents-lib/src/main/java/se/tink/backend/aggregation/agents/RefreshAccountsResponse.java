package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.aggregation.rpc.Account;

public class RefreshAccountsResponse {
    List<Account> refreshedList;

    public List<Account> getRefreshedList() {
        return refreshedList;
    }

    public void setRefreshedList(List<Account> refreshedList) {
        this.refreshedList = refreshedList;
    }
}
