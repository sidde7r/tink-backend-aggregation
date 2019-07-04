package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Response {

    private List<BalancesItem> balances;

    private Account account;

    public List<BalancesItem> getBalances() {
        return balances;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "Response{"
                + "balances = '"
                + balances
                + '\''
                + ",account = '"
                + account
                + '\''
                + "}";
    }
}
