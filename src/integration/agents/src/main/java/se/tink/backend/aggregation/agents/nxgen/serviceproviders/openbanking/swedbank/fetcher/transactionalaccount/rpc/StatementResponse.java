package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.Links;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StatementResponse {

    @JsonProperty("_links")
    private Links links;

    private String statementId;

    private String statementStatus;

    public Links getLinks() {
        return links;
    }

    public String getStatementId() {
        return statementId;
    }

    public String getStatementStatus() {
        return statementStatus;
    }
}
