package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.nordea;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class NordeaSeBusinessConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String BASE_URL = "https://open.nordea.com/business";
        public static final URL DECOUPLED_AUTHENTICATION =
                new URL(BASE_URL + ApiService.DECOUPLED_AUTHENTICATION);
        public static final URL DECOUPLED_AUTHORIZATION =
                new URL(BASE_URL + ApiService.DECOUPLED_AUTHORIZATION);
        public static final URL DECOUPLED_TOKEN = new URL(BASE_URL + ApiService.DECOUPLED_TOKEN);

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ApiService {
            public static final String DECOUPLED_AUTHENTICATION = "/v5/decoupled/authentications";
            public static final String DECOUPLED_AUTHORIZATION = "/v5/decoupled/authorizations";
            public static final String DECOUPLED_TOKEN = "/v5/decoupled/token";
        }
    }
}
