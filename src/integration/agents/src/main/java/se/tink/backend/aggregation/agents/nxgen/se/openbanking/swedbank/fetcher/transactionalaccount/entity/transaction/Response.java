package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Response {

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

    @Override
    public String toString() {
        return "Response{"
                + "_links = '"
                + links
                + '\''
                + ",statementId = '"
                + statementId
                + '\''
                + ",statementStatus = '"
                + statementStatus
                + '\''
                + "}";
    }
}
