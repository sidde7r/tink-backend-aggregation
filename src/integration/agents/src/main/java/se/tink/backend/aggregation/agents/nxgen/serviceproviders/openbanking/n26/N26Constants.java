package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class N26Constants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Url {
        static final String TOKEN_REQUEST = "/token-requests";
        static final String TOKEN_INFO = "/tokens/{tokenId}";
        static final String ACCOUNTS = "/accounts";
        static final String ACCOUNT_BALANCE = "/accounts/{accountId}/balance";
        static final String ACCOUNT_TRANSACTIONS = "/accounts/{accountId}/transactions";
        static final String TRANSFERS = "/transfers";
        static final String TRANSFER_DETAILS = "/transfers/{transferId}";
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
}
