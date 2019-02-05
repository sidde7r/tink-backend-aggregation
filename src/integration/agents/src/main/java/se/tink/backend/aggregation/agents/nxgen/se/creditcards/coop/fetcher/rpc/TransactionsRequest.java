package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {

    private int maxNrOfTransactions;
    private int accountType;
    private int fromYear;

    public static TransactionsRequest create(int maxNrOfTransactions, int accountType, int fromYear) {
        TransactionsRequest request = new TransactionsRequest();

        request.maxNrOfTransactions = maxNrOfTransactions;
        request.accountType = accountType;
        request.fromYear = fromYear;

        return request;
    }
}
