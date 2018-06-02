package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;
import se.tink.backend.utils.StringUtils;

public class SwedbankBaseConstants {

    public static class TransferScope {
        public static final String PAYMENT_FROM = "PAYMENT_FROM";
        public static final String TRANSFER_FROM = "TRANSFER_FROM";
    }

    public static class Description {
        private static final Splitter CLEANUP_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();
        private static final Joiner CLEANUP_JOINER = Joiner.on(' ');

        public static String clean(String description) {
            return AgentParsingUtils.cleanDescription(CLEANUP_JOINER.join(CLEANUP_SPLITTER.split(description)));
        }

        public static final ImmutableSet<String> PENDING_TRANSACTIONS = ImmutableSet.of("ÖVF VIA INTERNET",
                "SKYDDAT BELOPP");
    }


    public enum Url implements UrlEnum {
        INIT_BANKID(createUrlWithHost("/v5/identification/bankid/mobile"));

        public static final String DSID_KEY = "dsid";
        private URL url;

        Url(String url) {
            this.url = new URL(url);
        }

        @Override
        public URL get() {
            return url;
        }

        @Override
        public URL parameter(String key, String value) {
            return url.parameter(key, value);
        }

        @Override
        public URL queryParam(String key, String value) {
            return url.queryParam(key, value);
        }

        private static final String HOST = "https://auth.api.swedbank.se/TDE_DAP_Portal_REST_WEB/api";

        private static String createUrlWithHost(String path) {
            return HOST + path;
        }

        public static URL createDynamicUrl(String path) {
            return new URL(createUrlWithHost(path));
        }

        public static URL createDynamicUrl(String path, Map<String, String> parameters) {
            if (parameters == null || parameters.isEmpty()) {
                return createDynamicUrl(path);
            }

            URL url = new URL(createUrlWithHost(path));

            for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
                url = url.parameter(parameterEntry.getKey(), parameterEntry.getValue());
            }

            return url;
        }
    }

    public static class ParameterKey {
        public static final String FUND_CODE = "aFundCode";
    }

    public static String generateAuthorization(SwedbankConfiguration configuration, String username) {
        String deviceId = StringUtils.hashAsUUID("TINK-" + configuration.getName() + "-" + username);
        String apiKey = configuration.getApiKey();

        Base64 base64 = new Base64(100, null, true);

        return new String(base64.encode((apiKey + ":" + deviceId).getBytes(Charsets.US_ASCII)));
    }

    public static class Headers {
        public static final String AUTHORIZATION_KEY = "Authorization";
    }

    public static class StorageKey {
        public static final String NEXT_LINK = "nextLink";
        public static final String CREDIT_CARD_RESPONSE = "creditCardResponse";
    }

    public enum MenuItemKey {
        ACCOUNTS("EngagementOverview"), UPCOMING_TRANSACTIONS("UpcomingTransactions"), LOANS("LendingLoanOverview"),
        PORTFOLIOS("PortfolioHoldings"), FUND_MARKET_INFO("FundMarketinfo"), EINVOICES("EinvoiceIncoming"),
        PAYMENT_BASEINFO("PaymentBaseinfo");

        private String key;

        MenuItemKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static class LogTags {
        public static final LogTag LOAN_RESPONSE = LogTag.from("Swedbank loan response");
        public static final LogTag MORTGAGE_OVERVIEW_RESPONSE = LogTag.from("Swedbank mortgage overview");
        public static final LogTag LOAN_DETAILS_RESPONSE = LogTag.from("Swedbank loan details response");
        public static final LogTag DETAILED_PORTFOLIO_RESPONSE = LogTag.from(
                "Swedbank detailed portfolio - type:[{}] - response: {}");
        public static final LogTag PORTFOLIO_HOLDINGS_RESPONSE = LogTag.from("Portfolio holdings response: {}");
    }
}
