package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration;

import java.time.LocalDate;

public interface AspspConfiguration {
    /** ASPSP code used in path */
    String getAspspCode();

    /** Send withBalance=true when requesting account list. */
    boolean shouldRequestAccountsWithBalance();

    /** True if the ASPSP supports requesting transactions with bookingStatus=both */
    boolean supportsPendingTransactions();

    /** Date to request transactions from */
    LocalDate transactionsFromDate();
}
