package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsLinksEntity {

    @JsonProperty("account")
    private JsonNode accountInternal;

    @JsonProperty("next")
    private JsonNode nextInternal;

    @JsonProperty("last")
    private JsonNode lastInternal;

    @JsonProperty("first")
    private JsonNode firstInternal;

    private String account;
    private String first;
    private String last;
    private String next;
    private String transactionDetails;

    public String getAccount() {
        if (account == null && accountInternal != null) {
            return getUrlFromNode(accountInternal);
        }
        return account;
    }

    public String getFirst() {
        if (first == null && firstInternal != null) {
            return getUrlFromNode(firstInternal);
        }
        return first;
    }

    public String getLast() {
        if (last == null && lastInternal != null) {
            return getUrlFromNode(lastInternal);
        }
        return last;
    }

    public String getNext() {
        if (next == null && nextInternal != null) {
            return getUrlFromNode(nextInternal);
        }
        return next;
    }

    private String getUrlFromNode(JsonNode node) {
        if (node.isObject()) {
            return node.get(Transactions.HREF).asText();
        } else if (node.isTextual()) {
            return node.asText();
        } else {
            throw new IllegalStateException(ErrorMessages.PARSING_URL);
        }
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }
}
