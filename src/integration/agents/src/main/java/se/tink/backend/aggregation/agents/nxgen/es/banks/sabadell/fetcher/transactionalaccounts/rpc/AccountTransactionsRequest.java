package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountTransactionsRequest {
    private final boolean moreRequest;
    private final AccountEntity account;
    private final String dateFrom;
    private final String dateTo;

    private AccountTransactionsRequest(Builder builder) {
        this.moreRequest = builder.moreRequest;
        this.account = builder.account;
        this.dateFrom = builder.dateFrom;
        this.dateTo = builder.dateTo;
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private static final DateTimeFormatter LOCAL_DATE_FORMAT =
                DateTimeFormatter.ofPattern("dd-MM-yyyy");
        private boolean moreRequest;
        private AccountEntity account;
        private String dateFrom;
        private String dateTo;

        public Builder moreRequest(boolean moreRequest) {
            this.moreRequest = moreRequest;
            return this;
        }

        public Builder account(AccountEntity account) {
            this.account = account;
            return this;
        }

        public Builder dateFrom(LocalDate from) {
            this.dateFrom = from.format(LOCAL_DATE_FORMAT);
            return this;
        }

        public Builder dateTo(LocalDate to) {
            this.dateTo = to.format(LOCAL_DATE_FORMAT);
            return this;
        }

        public AccountTransactionsRequest build() {
            return new AccountTransactionsRequest(this);
        }
    }
}
