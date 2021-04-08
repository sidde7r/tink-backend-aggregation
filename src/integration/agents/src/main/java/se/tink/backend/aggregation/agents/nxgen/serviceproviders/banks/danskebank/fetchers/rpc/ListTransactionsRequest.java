package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode
public final class ListTransactionsRequest {
    private final String languageCode;
    private final String accountNoInt;
    private final String bookingDateFrom;
    private final String bookingDateTo;
    private final boolean fetchPcatDetails = true;
    private final long minNumberToReturn = 9999;
    @Setter private String repositionKey = "";

    private ListTransactionsRequest(
            String languageCode,
            String accountNoInt,
            String bookingDateFrom,
            String bookingDateTo) {
        this.languageCode = languageCode;
        this.accountNoInt = accountNoInt;
        this.bookingDateFrom = bookingDateFrom;
        this.bookingDateTo = bookingDateTo;
    }

    public static ListTransactionsRequest create(
            String languageCode,
            String accountNoInt,
            String bookingDateFrom,
            String bookingDateTo) {
        return new ListTransactionsRequest(
                languageCode, accountNoInt, bookingDateFrom, bookingDateTo);
    }
}
