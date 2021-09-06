package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("balances")
    private JsonNode balancesInternal;

    @JsonProperty("transactions")
    private JsonNode transactionsInternal;

    @JsonProperty("account")
    private JsonNode accountInternal;

    private String balances;
    private String transactions;
    private String account;

    public String getBalances() {
        if (balances == null && balancesInternal != null) {
            return getUrlFromNode(balancesInternal);
        }
        return balances;
    }

    public String getTransactions() {
        if (transactions == null && transactionsInternal != null) {
            return getUrlFromNode(transactionsInternal);
        }
        return transactions;
    }

    public String getAccount() {
        if (account == null && accountInternal != null) {
            return getUrlFromNode(accountInternal);
        }
        return account;
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
}
