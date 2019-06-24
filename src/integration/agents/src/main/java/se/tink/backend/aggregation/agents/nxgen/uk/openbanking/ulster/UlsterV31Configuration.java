package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ulster;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Configuration;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UlsterV31Configuration extends UkOpenBankingV31Configuration {

    private static final URL BASE_URL = new URL("https://api.ulsterbank.co.uk/open-banking/v3.1");
    private static final URL ACCOUNT_BULK_REQUEST = BASE_URL.concat("/aisp/accounts");
    private static final String ACCOUNT_BALANCE_REQUEST = "/aisp/accounts/%s/balances";
    private static final String ACCOUNT_TRANSACTIONS_REQUEST = "/aisp/accounts/%s/transactions";
    private static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST =
            "/aisp/accounts/%s/scheduled-payments";

    /**
     * @param apiBaseUrl is ignored till the time we refactor the whole code and migrate the base
     *     url configurations for other agents too. (same is true for other methods too in this
     *     class.)
     */
    @Override
    public URL getBulkAccountRequestURL(URL apiBaseUrl) {
        return ACCOUNT_BULK_REQUEST;
    }

    @Override
    public URL getAccountBalanceRequestURL(URL apiBaseUrl, String accountId) {
        return BASE_URL.concat(String.format(ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    @Override
    public URL getUpcomingTransactionRequestURL(URL apiBaseUrl, String accountId) {
        return BASE_URL.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }
}
