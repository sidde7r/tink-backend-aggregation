package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import agents_platform_agents_framework.org.springframework.http.HttpHeaders;
import agents_platform_agents_framework.org.springframework.web.util.UriBuilder;
import agents_platform_agents_framework.org.springframework.web.util.UriComponentsBuilder;
import java.util.UUID;
import java.util.function.Supplier;

public final class MetroServiceConstants {
    public static final String HEADER_VERSION = "9.7.0.2004";
    public static final String TS_CLIENT_VERSION = "4.3.6;[1,2,3,6,7,8,10,11,12,14,28,19]";
    public static final String CONTENT_SIGNATURE = "Content-Signature";
    public static final String LOCALE_QUERY_PARAM = "locale";
    public static final String EN_US = "en-US";
    public static final String APPLICATION_ID_QUERY_PARAM = "aid";
    public static final String MOBILE_METRO_APPLICATION = "mobile_metro";

    private MetroServiceConstants() {
        throw new UnsupportedOperationException();
    }

    public enum Services {
        TULIP_SERVICE(URL.TULIP_BASE_URL, CommonHeaders.TULIP_HEADERS),
        MOBILE_APP_SERVICE(URL.MOBILE_APP_BASE_URL, CommonHeaders.MOBILE_APP_HEADERS),
        AUTHENTICATION_SERVICE(URL.AUTHENTICATION_BASE_URL, CommonHeaders.AUTHENTICATION_HEADERS);

        private final URL url;

        private final CommonHeaders headers;

        Services(URL url, CommonHeaders headers) {
            this.url = url;
            this.headers = headers;
        }

        public UriBuilder url() {
            return UriComponentsBuilder.fromUriString(url.getBaseUrl());
        }

        public HttpHeaders defaultHeaders() {
            return this.headers.getHeaders();
        }

        enum URL {
            TULIP_BASE_URL("https://tulips.metrobankonline.co.uk/fp/"),
            MOBILE_APP_BASE_URL(
                    "https://mobileapp.metrobankonline.co.uk/portalserver/services/rest/retail/"),
            AUTHENTICATION_BASE_URL("https://tlc.metrobankonline.co.uk:443/api/v2/auth/");

            private final String baseUrl;

            URL(String baseUrl) {
                this.baseUrl = baseUrl;
            }

            public String getBaseUrl() {
                return baseUrl;
            }
        }

        public enum CommonHeaders {
            TULIP_HEADERS(
                    () -> {
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.add(
                                "Cookie",
                                "thx_guid=" + UUID.randomUUID().toString().replace("-", ""));
                        return httpHeaders;
                    }),
            AUTHENTICATION_HEADERS(
                    () -> {
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.add(
                                "Authorization",
                                "TSToken d86ee7a8-ef14-4144-ba1d-f7a2b4c29e18; tid=mobile-retail-key");
                        httpHeaders.add("X-TS-Client-Version", TS_CLIENT_VERSION);
                        httpHeaders.add(
                                "User-Agent",
                                "MetroBankMobile/9.7.0little (iPhone; iOS 12.4.5; Scale/2.00)");
                        return httpHeaders;
                    }),
            MOBILE_APP_HEADERS(
                    () -> {
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.add("X-APP-VERSION", HEADER_VERSION);
                        httpHeaders.add("X-PLATFORM", GlobalConstants.PLATFORM.getValue());
                        httpHeaders.add(
                                "User-Agent",
                                "MetroBankMobile/9.7.0 (uk.plc.metrobankmobile.ios; build:2004; iOS 12.4.5) Alamofire/9.7.0");
                        return httpHeaders;
                    });

            private final HttpHeaders headers;

            CommonHeaders(Supplier<HttpHeaders> headersSupplier) {
                this.headers = headersSupplier.get();
            }

            public HttpHeaders getHeaders() {
                return headers;
            }
        }
    }
}
