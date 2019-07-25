package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration;

public interface AspspConfiguration {
    /** ASPSP code used in path */
    String getAspspCode();

    /** Send withBalance=true when requesting account list. */
    boolean shouldRequestAccountsWithBalance();

    /**
     * If true, will request the first page of transactions from the transaction link in the account
     * entity, or without any parameters. Otherwise, date pagination is assumed.
     */
    boolean supportsTransactionKeyPagination();

    /** True if the ASPSP supports requesting transactions with bookingStatus=both */
    boolean supportsPendingTransactions();
}
