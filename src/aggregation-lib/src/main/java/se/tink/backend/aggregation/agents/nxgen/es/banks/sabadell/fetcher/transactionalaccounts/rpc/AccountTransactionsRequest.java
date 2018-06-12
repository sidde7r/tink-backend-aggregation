package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountTransactionsRequest {
    private boolean moreRequest;
    private AccountEntity account;
    private String dateFrom;
    private String dateTo;

    @JsonIgnore
    public static AccountTransactionsRequest build(AccountEntity account, boolean moreRequest) {
        AccountTransactionsRequest accountTransactionsRequest = new AccountTransactionsRequest();

        accountTransactionsRequest.moreRequest = moreRequest;
        accountTransactionsRequest.account = account;
        accountTransactionsRequest.dateFrom = "";
        accountTransactionsRequest.dateTo = "";

        return accountTransactionsRequest;
    }

    public boolean isMoreRequest() {
        return moreRequest;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }
}
