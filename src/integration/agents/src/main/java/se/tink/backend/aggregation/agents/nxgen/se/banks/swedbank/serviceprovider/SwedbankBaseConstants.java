package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.BankTransferConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.strings.StringUtils;

public class SwedbankBaseConstants {
    public static class TransferScope {
        public static final String PAYMENT_FROM = "PAYMENT_FROM";
        public static final String TRANSFER_FROM = "TRANSFER_FROM";
        public static final String TRANSFER_TO = "TRANSFER_TO";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static class Description {
        private static final Splitter CLEANUP_SPLITTER =
                Splitter.on(CharMatcher.whitespace()).omitEmptyStrings();
        private static final Joiner CLEANUP_JOINER = Joiner.on(' ');

        public static String clean(String description) {
            return AgentParsingUtils.cleanDescription(
                    CLEANUP_JOINER.join(CLEANUP_SPLITTER.split(description)));
        }

        public static final ImmutableSet<String> PENDING_TRANSACTIONS =
                ImmutableSet.of("ÖVF VIA INTERNET", "SKYDDAT BELOPP");
    }

    public enum Url {
        INIT_BANKID("/v5/identification/bankid/mobile"),
        INIT_TOKEN("/v5/identification/securitytoken/challenge"),
        TOUCH("/v5/identification/touch"),
        LOGOUT("/v5/identification/logout"),
        PROFILE("/v5/profile/");

        public static final String IDENTIFICATION = "/v5/identification";
        private String path;

        Url(String path) {
            this.path = path;
        }

        public URL get(String host) {
            return new URL(createUrlWithHost(host, path));
        }

        public String getPath() {
            return path;
        }

        private static String createUrlWithHost(String host, String path) {
            return host + path;
        }

        public static URL createDynamicUrl(String host, String path) {
            return new URL(createUrlWithHost(host, path));
        }

        public static URL createDynamicUrl(
                String host, String path, Map<String, String> parameters) {
            if (parameters == null || parameters.isEmpty()) {
                return createDynamicUrl(host, path);
            }

            URL url = new URL(createUrlWithHost(host, path));

            for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
                url = url.parameter(parameterEntry.getKey(), parameterEntry.getValue());
            }

            return url;
        }
    }

    public static class ParameterKey {
        public static final String FUND_CODE = "aFundCode";
    }

    public static String generateAuthorization(
            SwedbankConfiguration configuration, String username) {
        String deviceId =
                StringUtils.hashAsUUID("TINK-" + configuration.getName() + "-" + username);
        String apiKey = configuration.getApiKey();

        Base64 base64 = new Base64(100, null, true);

        return new String(base64.encode((apiKey + ":" + deviceId).getBytes(Charsets.US_ASCII)));
    }

    public static class Headers {
        public static final String AUTHORIZATION_KEY = "Authorization";
    }

    public static class StorageKey {
        public static final String NEXT_LINK = "nextLink";
        public static final String PROFILE = "profile";
        public static final String CREDIT_CARD_RESPONSE = "creditCardResponse";
        public static final String BANK_PROFILE_HANDLER = "BANK_PROFILES";
        public static final String ID = "id";
    }

    public static class SavingAccountTypes {
        public static final String PENSION = "pension";
    }

    // Some investment accounts are returned in the same list as savings accounts, but they should
    // be filtered out so they are not aggregated as savings. Found these product IDs for savings
    // accounts that always match an investment account.
    public static final Set<String> INVESTMENT_ACCOUNT_PRODUCT_IDS =
            ImmutableSet.of(
                    "DIRPDEPA",
                    "DIRPFOND",
                    "FOND",
                    "FSP002",
                    "FVR",
                    "ISKPRV01",
                    "KAPPFOND",
                    "KAPSFOND",
                    "KBRNFOND",
                    "KFORDEPA",
                    "KPENFOND");

    public enum InvestmentAccountType {
        EQUITY_TRADER("EQUITY_TRADER"),
        SAVINGSACCOUNT("SAVINGSACCOUNT"),
        ISK("ISK"),
        FUNDACCOUNT("FUNDACCOUNT"),
        ENDOWMENT("ENDOWMENT"),
        OCCUPATIONAL_PENSION("OCCUPATIONAL_PENSION"),
        INDIVIDUAL_SAVINGS_PENSION("INDIVIDUAL_SAVINGS_PENSION"),
        UNKNOWN("");

        private String accountType;

        InvestmentAccountType(String accountType) {
            this.accountType = accountType;
        }

        public String getAccountType() {
            return accountType;
        }

        public static InvestmentAccountType fromAccountType(String accountType) {
            String type = Optional.ofNullable(accountType).orElse("");

            return Arrays.stream(InvestmentAccountType.values())
                    .filter(
                            investmentAccountType ->
                                    investmentAccountType.getAccountType().equalsIgnoreCase(type))
                    .findFirst()
                    .orElse(InvestmentAccountType.UNKNOWN);
        }
    }

    public enum BankIdResponseStatus {
        CLIENT_NOT_STARTED("CLIENT_NOT_STARTED"),
        USER_SIGN("USER_SIGN"),
        COMPLETE("COMPLETE"),
        CANCELLED("CANCELLED"),
        TIMEOUT("TIMEOUT"),
        ALREADY_IN_PROGRESS("OUTSTANDING_TRANSACTION"),
        INTERRUPTED("CANCELLED_BY_NEW_INIT_AUTHENTICATION"),
        UNKNOWN("");

        private String statusCode;

        BankIdResponseStatus(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public static BankIdResponseStatus fromStatusCode(String statusCode) {
            return Arrays.stream(BankIdResponseStatus.values())
                    .filter(
                            bankIdStatus ->
                                    bankIdStatus.getStatusCode().equalsIgnoreCase(statusCode))
                    .findFirst()
                    .orElse(BankIdResponseStatus.UNKNOWN);
        }
    }

    public enum Authorization {
        AUTHORIZED("AUTHORIZED"),
        REQUIRES_AUTH_METHOD_CHANGE("REQUIRES_AUTH_METHOD_CHANGE"),
        UNAUTHORIZED("UNAUTHORIZED"),
        UNKNOWN("");

        private String authRequirement;

        Authorization(String authRequirement) {
            this.authRequirement = authRequirement;
        }

        public String getAuthRequirement() {
            return authRequirement;
        }

        public static Authorization fromAuthorizationString(String authorization) {
            String auth = Optional.ofNullable(authorization).orElse("");

            return Arrays.stream(Authorization.values())
                    .filter(
                            authorizationEnum ->
                                    authorizationEnum.getAuthRequirement().equalsIgnoreCase(auth))
                    .findFirst()
                    .orElse(Authorization.UNKNOWN);
        }
    }

    public enum LinkMethod {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        OPTIONS("OPTIONS"),
        DELETE("DELETE"),
        HEAD("HEAD"),
        UNKNOWN("");

        private String verb;

        LinkMethod(String verb) {
            this.verb = verb;
        }

        public String getVerb() {
            return verb;
        }

        public static LinkMethod fromVerb(String verb) {
            String auth = Optional.ofNullable(verb).orElse("");

            return Arrays.stream(LinkMethod.values())
                    .filter(linkMethod -> linkMethod.getVerb().equalsIgnoreCase(verb))
                    .findFirst()
                    .orElse(LinkMethod.UNKNOWN);
        }
    }

    public static final class DeviceAuthentication {
        public static final String CHALLENGE = "challenge";
    }

    public enum MenuItemKey {
        ACCOUNTS("EngagementOverview"),
        UPCOMING_TRANSACTIONS("UpcomingTransactions"),
        LOANS("LendingLoanOverview"),
        PORTFOLIOS("PortfolioHoldings"),
        PENSION_PORTFOLIOS("PortfolioPensions"),
        FUND_MARKET_INFO("FundMarketinfo"),
        EINVOICES("EinvoiceIncoming"),
        PAYMENT_BASEINFO("PaymentBaseinfo"),
        PAYMENT_REGISTERED("PaymentRegistered"),
        REGISTER_TRANSFER("PaymentRegisterTransfer"),
        REGISTER_PAYMENT("PaymentRegisterPayment"),
        PAYMENTS_CONFIRMED("PaymentConfirmed"),
        REGISTER_PAYEE("PaymentRegisterPayee"),
        REGISTER_EXTERNAL_TRANSFER_RECIPIENT("PaymentRegisterExternalRecipient");

        private String key;

        MenuItemKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static class LogTags {
        public static final LogTag FUND_MISSING_ISIN =
                LogTag.from("Swedbank fund missing ISIN code, holding name: {}");
    }

    public static class BankErrorMessage {
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
        public static final String SESSION_INVALIDATED = "SESSION_INVALIDATED";
    }

    public static class ErrorMessage {
        public static final String INVALID_DESTINATION =
                BankTransferConstants.ErrorMessage.INVALID_DESTINATION;
        public static final String INVALID_DESTINATION_MESSAGE =
                "Destination message can contain digits, letters in Swedish alphabet, or spaces.";
        public static final String INVALID_SOURCE =
                BankTransferConstants.ErrorMessage.INVALID_SOURCE;
        public static final String SOURCE_NOT_FOUND = "Source account could not be found at bank.";
        public static final String SOURCE_NOT_TRANSFER_CAPABLE =
                "Source account not allowed to to make transfers.";
        public static final String TRANSFER_REGISTER_FAILED = "Could not register transfer.";
        public static final String TRANSFER_CONFIRM_FAILED =
                "Could not confirm transfer was executed.";
        public static final String COLLECT_BANKID_FAILED =
                "Failed when collecting bankid for signing transfer.";
        public static final String COLLECT_BANKID_TIMEOUT =
                "Timeout when collecting bankID for signing transfer";
        public static final String COLLECT_BANKID_CANCELLED =
                "Could not confirm transfer with BankID signing.";
        public static final String TOKEN_SIGN_FAILED = "Could not verify security token.";
        public static final String NOT_EXACTLY_ONE_UNSIGNED_TRANSFER =
                "Number of unsigned transfers not equal to one - Cancelling to not sign more than one transfer.";
        public static final String UNSIGNED_TRANFERS =
                "Existing unsigned transfers - Cancelling to not sign more than one transfer.";
        public static final String NEEDS_EXTENDED_USE =
                "Activation of extended use for BankId required";
        public static final String CONNECT_TIMEOUT = "connect timed out";
        public static final String CHALLENGE_NO_RESPONSE =
                "Transfer or payment was not signed with security token device";
        public static final String OCR_NOT_ALLOWED =
                "Den valda mottagaren tillåter inte betalningar med OCR-nummer, vänligen välj meddelande.";
        public static final String DUPLICATE_PAYMENT =
                "The payment could not be made because an identical payment is already registered";
    }

    public static class UserMessage {
        public static final LocalizableKey STRONGER_AUTHENTICATION_NEEDED =
                new LocalizableKey(
                        "In order to add new recipients you need to activate Mobile BankID for extended use. You can find more information on how to extend your Mobile BankID in your Internet bank.");
        public static final LocalizableKey WRONG_BANK_SWEDBANK =
                new LocalizableKey(
                        "You do not have any accounts at Swedbank. Use Sparbankerna (Mobile BankID) instead.");
        public static final LocalizableKey WRONG_BANK_SAVINGSBANK =
                new LocalizableKey(
                        "You do not have any accounts at Sparbankerna. Use Swedbank (Mobile BankID) instead.");
    }

    public static class ErrorCode {
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String NOT_ALLOWED = "NOT_ALLOWED";
        public static final String AUTHORIZATION_FAILED = "AUTHORIZATION_FAILED";
        public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
        public static final String STRONGER_AUTHENTICATION_NEEDED =
                "STRONGER_AUTHENTICATION_NEEDED";
        public static final String DUPLICATION = "DUPLICATE";
        public static final String TRANSFER_ALREADY_EXISTS = "TRANSFER_ALREADY_EXISTS";
    }

    public static class ErrorField {
        public static final String DATE = "date";
        public static final String USER_ID = "userid";
        public static final String RECIPIENT_NUMBER = "recipientnumber";
        public static final String REFERENCE = "reference";
        public static final String RESPONSE = "response";
    }

    public static class TransactionType {
        public static final String TRANSFER = "transfer";
        public static final String PAYMENT = "payment";
    }

    public static class PaymentType {
        public static final String DOMESTIC = "DOMESTIC";
    }

    public static class PaymentStatus {
        public static final String UNDER_WAY = "UNDER_WAY";
    }

    public static class PaymentDateDependency {
        public static final String DIRECT = "DIRECT";
    }

    public static class PaymentAccountType {
        public static final String BGACCOUNT = "BGACCOUNT";
        public static final String PGACCOUNT = "PGACCOUNT";
    }

    public static class TransferRecipientType {
        public static final String BANKACCOUNT = "BANKACCOUNT";
    }

    public static class BankId {
        public static final int MAX_ATTEMPTS = 90;
        public static final int BANKID_SLEEP_INTERVAL = 2000;
    }

    // Temporary constants while Swedbank is having problems with their pagination
    public static class PaginationError {
        public static final String PAGINATION_ERROR = "PAGINATION_ERROR";
        public static final String PAGINATION_ERROR_MSG = "Error_paginating_transactions ";
    }

    public static class Retry {
        public static final int FIRST_ATTEMPT = 1;
        public static final int MAX_RETRY_ATTEMPTS = 2;
    }
}
