package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonAlias("transactionDetails")
    private LinksDetailsEntity transactions;

    public LinksDetailsEntity getTransactions() {
        return transactions;
    }
}
