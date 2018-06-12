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

    public static class BankIdStatus {
        public static final String USER_SIGN = "USER_SIGN";
        public static final String COMPLETE = "COMPLETE";
    }

    public enum MenuItemKey {
        ACCOUNTS("EngagementOverview"), UPCOMING_TRANSACTIONS("UpcomingTransactions"), LOANS("LendingLoanOverview"),
        PORTFOLIOS("PortfolioHoldings"), FUND_MARKET_INFO("FundMarketinfo"), EINVOICES("EinvoiceIncoming"),
        PAYMENT_BASEINFO("PaymentBaseinfo"), PAYMENT_REGISTERED("PaymentRegistered"),
        REGISTER_TRANSFER("PaymentRegisterTransfer"), REGISTER_PAYMENT("PaymentRegisterPayment"),
        PAYMENTS_CONFIRMED("PaymentConfirmed");

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

    public static class ErrorMessage {
        public static final String INVALID_DESTINATION = "Transfer failed due to invalid destination account.";
        public static final String INVALID_SOURCE = "Transfer failed due to invalid source account.";
        public static final String SOURCE_NOT_FOUND = "Source account could not be found at bank.";
        public static final String SOURCE_NOT_TRANSFER_CAPABLE = "Source account not allowed to to make transfers.";
        public static final String TRANSFER_REGISTER_FAILED = "Could not register transfer.";
        public static final String TRANSFER_CONFIRM_FAILED = "Could not confirm transfer was executed.";
        public static final String COLLECT_BANKID_FAILED = "Failed when collecting bankid for signing transfer.";
        public static final String COLLECT_BANKID_CANCELLED = "Could not confirm transfer with BankID signing.";
        public static final String NOT_EXACTLY_ONE_UNSIGNED_TRANSFER = "Number of unsigned transfers not equal to one - Cancelling to not sign more than one transfer.";
        public static final String UNSIGNED_TRANFERS = "Existing unsigned transfers - Cancelling to not sign more than one transfer.";
        public static final String EINVOICE_NO_UNIQUE_ID = "Could not get unique id from transfer object.";
        public static final String EINVOICE_NO_MATCH = "Could not find a matching eInvoice.";
    }

    public static class TransactionType {
        public static final String TRANSFER = "transfer";
        public static final String PAYMENT = "payment";
    }
}
