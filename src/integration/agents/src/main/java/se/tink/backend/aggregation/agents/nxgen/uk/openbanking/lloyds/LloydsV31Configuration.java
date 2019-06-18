package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Configuration;
import se.tink.backend.aggregation.nxgen.http.URL;

public class LloydsV31Configuration extends UkOpenBankingV31Configuration {

    private static final String ACCOUNT_BULK_REQUEST = "/aisp/accounts";
    private static final String ACCOUNT_BALANCE_REQUEST = "/aisp/accounts/%s/balances";
    private static final String ACCOUNT_TRANSACTIONS_REQUEST = "/aisp/accounts/%s/transactions";
    private static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST =
            "/aisp/accounts/%s/scheduled-payments";

    @Override
    public URL getBulkAccountRequestURL(URL apiBaseUrl) {
        return apiBaseUrl.concat(ACCOUNT_BULK_REQUEST);
    }

    @Override
    public URL getAccountBalanceRequestURL(URL apiBaseUrl, String accountId) {
        return apiBaseUrl.concat(String.format(ACCOUNT_BALANCE_REQUEST, accountId));
    }

    @Override
    public String getInitialTransactionsPaginationKey(String accountId) {
        return String.format(ACCOUNT_TRANSACTIONS_REQUEST, accountId);
    }

    @Override
    public URL getUpcomingTransactionRequestURL(URL apiBaseUrl, String accountId) {
        return apiBaseUrl.concat(String.format(ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST, accountId));
    }
}
