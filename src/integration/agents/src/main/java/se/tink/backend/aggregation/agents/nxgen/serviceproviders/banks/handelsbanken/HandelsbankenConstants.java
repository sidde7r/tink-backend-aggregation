package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities.DeviceInfoKeyValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.HandelsbankenValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.Linkable;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class HandelsbankenConstants {

    public static final SecureRandom RANDOM = new SecureRandom();
    public static final Base64 BASE64_CODEC = new Base64();
    public static final Pattern BANKID_PATTERN =
            Pattern.compile(
                    Joiner.on("|")
                            .join(
                                    "[0-9]{8,9}",
                                    "[0-9]{13}",
                                    "[0-9]{2}-[0-9]{6}-[0-9]{6}",
                                    "[0-9]{4}( \\*{4}){2} [0-9]{4}"));

    public static final class URLS {

        public enum Links implements Linkable {
            ACCOUNTS("accounts"),
            ACCOUNT_INFO("account-info"),
            ACTIVATE_PROFILE("activateProfile"),
            APPLICATION_ENTRY_POINT("application-entry-point"),
            APPLICATION_EXIT_POINT("application-exit-point"),
            APPROVAL("approval"),
            AUTHENTICATE("authenticate"),
            AUTHORIZE("authorize"),
            AUTHORIZE_MANDATE("authorizeMandate"),
            BANKID_LOGIN("bankid-login_2_0"),
            CARD_TRANSACTIONS("card-transactions"),
            CARD_MORE_TRANSACTIONS("card-more-transactions"),
            CARD_DEPOSIT_TRANSACTIONS("card-deposit-transactions"),
            CARDS("cards"),
            CHECK_AGREEMENT("checkAgreement"),
            COMMIT_PROFILE("commitProfile"),
            CONFIRM_EXECUTE("confirmexecute"),
            CONFIRM_SIGN("confirmsign"),
            CREATE("create"),
            CREATE_PINCODE("createPincode"),
            CREATE_PROFILE("createProfile"),
            CUSTODY_ACCOUNT("custody-account"),
            EINVOICE_DETAIL("einvoice-detail"),
            FUND_HOLDINGS("fund-holdings"),
            FUND_HOLDING_DETAILS("fund-holding-details"),
            GET_CHALLENGE("getChallenge"),
            GET_SERVER_PROFILE("getServerProfile"),
            HOLDINGS_SUMMARY("holdings-summary"),
            KEEP_ALIVE("keepalive"),
            LOANS("loans"),
            LOOKUP_RECIPIENT("lookup-recipient"),
            PAYMENT_CONTEXT("payment-context"),
            PAYMENT_DETAIL("payment-detail"),
            PENDING_EINVOICES("einvoice-pending"),
            PENDING_TRANSACTIONS("pending-transactions"),
            PENSION_DETAILS("pension-detail"),
            PENSION_OVERVIEW("pension-overview"),
            PINNED_ACTIVATION("pinned-activation"),
            PINNED_LOGIN("pinned-login"),
            SAVE_INVEST_START("save-invest-startpage"),
            SECURITY_HOLDING("security-holding"),
            SELF("self"),
            SIGNATURE("signature"),
            TRANSACTIONS("transactions"),
            TRANSFER_CONTEXT("transfer-context"),
            TRANSINFO("transinfo"),
            UPDATE("update"),
            VALIDATE_SIGNATURE("validateSignature"),
            VERIFY_SECURITY_CODE("verifySecurityCode"),
            VALIDATE_RECIPIENT("validate-recipient");

            private final String name;

            Links(String name) {
                this.name = name;
            }

            @Override
            public String getName() {
                return this.name;
            }
        }

        public static final class KeepAlive {
            public static final LogTag LOG_TAG = LogTag.from("#Handelsbanken_keep_alive");
        }

        public static final class Parameters {
            public static final String GIRO_NUMBER = "bgPgNumber";
        }
    }

    public static final class TransactionFiltering {
        public static final String CREDIT_CARD_SUMMARY = "periodens köp";
    }

    public static final class Headers {
        public static final String X_SHB_DEVICE_NAME = "X-SHB-DEVICE-NAME";
        public static final String DEVICE_NAME = "iOS;Tink;Tink";
        public static final String X_SHB_DEVICE_MODEL = "X-SHB-DEVICE-MODEL";
        public static final String X_SHB_DEVICE_CLASS = "X-SHB-DEVICE-CLASS";
        public static final String DEVICE_CLASS = "APP";
        public static final String X_SHB_APP_VERSION = "X-SHB-APP-VERSION";
    }

    public static final class Storage {
        public static final String PROFILE_ID = "profileId";
        public static final String PRIVATE_KEY = "privateKey";
        public static final String DEVICE_SECURITY_CONTEXT_ID = "deviceSecurityContextId";
        public static final String AUTHORIZE_END_POINT = "authorizeEndPoint";
        public static final String APPLICATION_ENTRY_POINT = "applicationEntryPoint";
        public static final String ACCOUNT_LIST = "accountList";
        public static final String CREDIT_CARDS = "creditCards";
        public static final String ORGANISATION_NUMBER = "organisationNumber";
    }

    public static final class Authentication {

        public static final List<DeviceInfoKeyValue> DEVICE_INFO_DATA =
                Lists.newArrayList(
                        new DeviceInfoKeyValue("app_version", "OC41LjA="), // 8.5.0
                        new DeviceInfoKeyValue("comp_ver", "Mg=="), // 2
                        // Jailbreak detection. Set manually to `0` in order to not set off any
                        // alarms (as of today it is OK to have
                        // these set to `1`).
                        new DeviceInfoKeyValue("i_jbd_001", "MA=="), // 0
                        new DeviceInfoKeyValue("i_jbd_002", "MA=="), // 0
                        new DeviceInfoKeyValue("i_jbd_101", "MA=="), // 0
                        new DeviceInfoKeyValue("i_model", "aVBob25l"), // iPhone
                        // This value looks to be static, could be application version specific.
                        new DeviceInfoKeyValue("i_sid", "sHM+0eLKRWSeFaeFxqKArA=="),
                        new DeviceInfoKeyValue("i_sid_f", "MA=="), // 0
                        new DeviceInfoKeyValue("os_name", "aU9T"), // iOS
                        new DeviceInfoKeyValue("os_version", "MTEuNC4x"), // 11.4.1
                        new DeviceInfoKeyValue("prof_trans_ver", "MQ=="));
    }

    public static final class DeviceAuthentication {

        public static final String CODE = "code";

        public enum OtherUserError {
            WRONG_CARD(
                    new LocalizableKey(
                            "Are you trying to use your charge card "
                                    + "to login? Please use your login card for BankID. The response should be 9 figures.")),
            CODE_ACTIVATION_NEEDED(
                    new LocalizableKey(
                            "You need to "
                                    + "activate your personal code for telephone and mobile services on the internet bank.")),
            PINCODE_CREATION_NEEDED(
                    new LocalizableKey(
                            "You need to activate your "
                                    + "mobile app credentials and generate a 4-digit pin code via the Handelsbanken app."));

            private final LocalizableKey key;

            OtherUserError(LocalizableKey key) {
                this.key = key;
            }

            public void throwException() throws LoginException {
                throw LoginError.INCORRECT_CREDENTIALS.exception(key);
            }
        }

        public enum BankCheckedUserError {
            INCORRECT_CREDENTIALS(
                    LoginError.INCORRECT_CREDENTIALS,
                    new LocalizableKey(
                            "You have used " + "incorrect credentials. Please try again."),
                    codePredicate("101")),
            WRONG_ACTIVATION_CODE(
                    LoginError.INCORRECT_CREDENTIALS,
                    new LocalizableKey(
                            "Incorrect answer code. The "
                                    + "challenge code is only active for "
                                    + "four minutes. Press OK to restart."),
                    codePredicate("102")),
            TOO_MANY_ACTIVATED_APPS(
                    AuthorizationError.ACCOUNT_BLOCKED,
                    new LocalizableKey(
                            "The activation could not "
                                    + "be completed. It is not possible to "
                                    + "activate more than ten apps."),
                    codePredicate("104")),
            TEMP_BLOCKED_CARD(
                    AuthorizationError.ACCOUNT_BLOCKED,
                    new LocalizableKey(
                            "Too many incorrect tries. The "
                                    + "card has been locked for 60 minutes "
                                    + "for signing with the card reader without a cord. Contact Handelsbanken technical support for "
                                    + "more information."),
                    codePredicate("103").and(messagePredicate("låst i 60 min för signering"))),
            PERM_BLOCKED_CARD(
                    AuthorizationError.ACCOUNT_BLOCKED,
                    new LocalizableKey(
                            "The card you are using is "
                                    + "blocked. Please contact Handelsbanken "
                                    + "technical support or visit your local bank office to order a new login card."),
                    codePredicate("103").and(messagePredicate("kortet är spärrat")));

            private final AgentError agentError;
            private final LocalizableKey key;
            private final Predicate<HandelsbankenValidator<BaseResponse>> isError;

            BankCheckedUserError(
                    AgentError agentError,
                    LocalizableKey key,
                    Predicate<HandelsbankenValidator<BaseResponse>> isError) {
                this.agentError = agentError;
                this.key = key;
                this.isError = isError;
            }

            private static Predicate<HandelsbankenValidator<BaseResponse>> codePredicate(
                    String errorCode) {
                return validator -> errorCode.equals(validator.getCode());
            }

            private static Predicate<HandelsbankenValidator<BaseResponse>> messagePredicate(
                    String s) {
                return validator -> {
                    String message = validator.getMessage();
                    return !Strings.isNullOrEmpty(message) && message.toLowerCase().contains(s);
                };
            }

            public static void throwException(
                    HandelsbankenValidator<BaseResponse> validator,
                    Supplier<RuntimeException> fallback)
                    throws AuthenticationException, AuthorizationException {
                for (BankCheckedUserError error : values()) {
                    if (error.isError.test(validator)) {
                        error.throwException();
                    }
                }
                throw fallback.get();
            }

            private void throwException() throws AuthenticationException, AuthorizationException {
                // Have to satisfy method signature...
                AgentException exception = agentError.exception(key);
                if (exception instanceof AuthenticationException) {
                    throw (AuthenticationException) exception;
                } else {
                    throw (AuthorizationException) exception;
                }
            }
        }
    }

    public static final class AutoAuthentication {

        public enum UserError {
            BLOCKED_DUE_TO_INACTIVITY(
                    SessionError.SESSION_EXPIRED,
                    new LocalizableKey(
                            "Handelsbanken is no longer active for the "
                                    + "specified user. "
                                    + "This could be due to that the Handelsbanken app for this device has not been active in 90 days. "
                                    + "To login again you need to activate the app again.")),
            DEVICE_SECURITY_CONTEXT_ID_INVALID(
                    SessionError.SESSION_EXPIRED, LoginError.INCORRECT_CREDENTIALS.userMessage()),
            INCORRECT_CREDENTIALS(
                    SessionError.SESSION_EXPIRED, LoginError.INCORRECT_CREDENTIALS.userMessage());
            private final AgentError agentError;
            private final LocalizableKey key;

            UserError(AgentError agentError, LocalizableKey key) {
                this.agentError = agentError;
                this.key = key;
            }

            public SessionException exception() {
                return (SessionException) agentError.exception(key);
            }
        }

        public static final class Validation {

            public static final String DEVICE_SECURITY_CONTEXT_ID_INVALID = "100";
            public static final String INACTIVE_USER_PROFILE = "107";
        }
    }
}
