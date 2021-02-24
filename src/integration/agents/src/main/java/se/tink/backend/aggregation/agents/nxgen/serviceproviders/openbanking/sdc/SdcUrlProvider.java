package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import lombok.Getter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Getter
public class SdcUrlProvider {

    private static final String BASE_AUTH_URL = "https://auth.sdc.dk";
    private static final String BASE_API_URL = "https://api-proxy.sdc.dk/api/psd2";

    private static final String AUTHORIZATION = "/Account/Login";
    private static final String TOKEN = "/Token";
    private static final String ACCOUNTS = "/v1/accounts";
    private static final String BALANCES = "/v1/accounts/{account-id}/balances";
    private static final String TRANSACTIONS = "/v1/accounts/{account-id}/transactions";

    private URL authorizationUrl;
    private URL tokenUrl;
    private URL accountsUrl;
    private URL balancesUrl;
    private URL transactionsUrl;

    public SdcUrlProvider() {
        this(BASE_AUTH_URL, BASE_API_URL);
    }

    SdcUrlProvider(String baseAuthUrl, String baseApiUrl) {
        this.authorizationUrl = new URL(baseAuthUrl + AUTHORIZATION);
        this.tokenUrl = new URL(baseAuthUrl + TOKEN);
        this.accountsUrl = new URL(baseApiUrl + ACCOUNTS);
        this.balancesUrl = new URL(baseApiUrl + BALANCES);
        this.transactionsUrl = new URL(baseApiUrl + TRANSACTIONS);
    }
}
