package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

public final class ListTransactionsRequest {
    private final String languageCode;
    private final String accountNoInt;
    private final String bookingDateFrom;
    private final String bookingDateTo;
    private final boolean fetchPcatDetails = true;
    private final long minNumberToReturn = 9999;
    private String repositionKey = "";

    private ListTransactionsRequest(String languageCode, String accountNoInt, String bookingDateFrom,
                                    String bookingDateTo) {
        this.languageCode = languageCode;
        this.accountNoInt = accountNoInt;
        this.bookingDateFrom = bookingDateFrom;
        this.bookingDateTo = bookingDateTo;
    }

    public static ListTransactionsRequest create(String languageCode, String accountNoInt, String bookingDateFrom,
                                                 String bookingDateTo) {
        return new ListTransactionsRequest(languageCode, accountNoInt, bookingDateFrom, bookingDateTo);
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getAccountNoInt() {
        return accountNoInt;
    }

    public String getBookingDateFrom() {
        return bookingDateFrom;
    }

    public String getBookingDateTo() {
        return bookingDateTo;
    }

    public boolean isFetchPcatDetails() {
        return fetchPcatDetails;
    }

    public long getMinNumberToReturn() {
        return minNumberToReturn;
    }

    public String getRepositionKey() {
        return repositionKey;
    }

    public void setRepositionKey(String repositionKey) {
        this.repositionKey = repositionKey;
    }
}
