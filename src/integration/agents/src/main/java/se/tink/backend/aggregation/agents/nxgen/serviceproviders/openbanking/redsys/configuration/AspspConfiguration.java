package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;

public interface AspspConfiguration {
    /** ASPSP code used in path */
    String getAspspCode();

    /** Send withBalance=true when requesting account list. */
    boolean shouldRequestAccountsWithBalance();

    /** True if the ASPSP supports requesting pending transactions with bookingStatus=pending */
    boolean supportsPendingTransactions();

    /**
     * Oldest allowed transaction date to fetch. Used on first transactions request. Might trigger
     * SCA flow for creating a new consent.
     */
    LocalDate oldestTransactionDate();

    /** Class to use for transactions response. This allows to customise transaction parsing. */
    Class<? extends BaseTransactionsResponse> getTransactionsResponseClass();
}
