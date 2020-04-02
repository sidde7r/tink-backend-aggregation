package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCardTransactionsRequest {
    private int requestedPage;
    private String cardNumber;

    private FetchCardTransactionsRequest(int requestedPage, String cardNumber) {
        this.requestedPage = requestedPage;
        this.cardNumber = cardNumber;
    }

    public static FetchCardTransactionsRequest of(int requestedPage, String cardNumber) {
        return new FetchCardTransactionsRequest(requestedPage, cardNumber);
    }
}
