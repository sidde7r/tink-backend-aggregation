package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class N26Constants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Url {
        static final String BASE_URL = "https://api.token.io";
        public static final String AUTHORIZATION_URL =
                "https://web-app.token.io/app/request-token/{tokenId}";
        static final String TOKEN_REQUEST = BASE_URL + "/token-requests";
        static final String TOKEN_INFO = BASE_URL + "/tokens/{tokenId}";
        static final String ACCOUNTS = BASE_URL + "/accounts";
        static final String ACCOUNT_BALANCE = BASE_URL + "/accounts/{accountId}/balance";
        static final String ACCOUNT_TRANSACTIONS = BASE_URL + "/accounts/{accountId}/transactions";
        static final String TRANSFERS = BASE_URL + "/transfers";
        static final String TRANSFER_DETAILS = BASE_URL + "/transfers/{transferId}";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class UrlParam {
        static final String TOKEN_ID = "tokenId";
        static final String ACCOUNT_ID = "accountId";
        static final String TRANSFER_ID = "transferId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Header {
        static final String AUTHORIZATION = "Authorization";
        static final String BASIC = "Basic ";
        static final String CONTENT_TYPE = "Content-Type";
        static final String ON_BEHALF_OF = "on-behalf-of";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Scope {
        public static final List<String> AIS =
                Collections.unmodifiableList(
                        Arrays.asList(
                                "ACCOUNTS",
                                "BALANCES",
                                "TRANSACTIONS",
                                "STANDING_ORDERS",
                                "TRANSFER_DESTINATIONS"));
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Timeout {
        public static final int TIMEOUT_IN_MILLISECONDS = 60_000;
        public static final int NUM_TIMEOUT_RETRIES = 2;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
