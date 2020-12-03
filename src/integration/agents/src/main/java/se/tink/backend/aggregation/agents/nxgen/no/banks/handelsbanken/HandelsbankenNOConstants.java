package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HandelsbankenNOConstants {
    public static final DeviceProfile DEVICE_PROFILE = DeviceProfileConfiguration.IOS_STABLE;

    public static final int AUTHENTICATION_TIMEOUT_COUNT =
            15; // set to 15 currently same as SparebankenSor
    public static final String APP_VERSION = "1.46.1";

    public enum Url implements UrlEnum {
        APP_INFORMATION(getNetbankEndpoint("/smbmobile/9055/appversion_ios.json")),
        VERIFY_CUSTOMER(
                getNetbankEndpoint(
                        "/secesb/rest/era/era/public/customers/%s/mobile?number=+47%s&orgid=9055")),
        CONFIGURE_BANKID(
                getNetbankEndpoint(
                        "/authenticate/login/bankidmobile?configKey=smbmactivate9055&userid=%s&phoneNumber=%s")),
        BANKID_1(
                getNetbankEndpoint(
                        "/authenticate/login/bankidmobile;jsessionid=%s?execution=e1s1")),
        BANKID_2(
                getNetbankEndpoint(
                        "/authenticate/login/bankidmobile;jsessionid=%s?execution=e1s2")),
        POLL_BANK(
                getNetbankEndpoint(
                        "/authenticate/login/rest/bankidmobilestatus.json;jsessionid=%s")),
        LOGIN_FIRST_STEP(getNetbankEndpoint("/secesb/rest/esb/v1/login")),
        LOGIN_SECOND_STEP(getNetbankEndpoint("/secesb/rest/era/login")),
        SEND_SMS(getNetbankEndpoint("/secesb/rest/era/sam/sms")),
        ACCOUNTS(getNetbankEndpoint("/secesb/rest/era/accounts")),
        TRANSACTIONS(
                getNetbankEndpoint(
                        "/secesb/rest/era%s?number=%s&include_authorizations=true&index=%s")),
        LOANS(
                getNetbankEndpoint(
                        "/secesb/rest/era-alp/loanmod/accounts?type=loan&includerepaymentplandetails=true")),
        LOAN_DETAILS(getNetbankEndpoint("/secesb/rest/era-alp%s")),
        USER_SETTINGS(getNetbankEndpoint("/secesb/rest/era/usersettings/SMARTbankMobile")),
        INIT_INVESTOR_LOGIN(getNetbankEndpoint("/secesb/rest/era/ssotoken/so")),
        CUSTOMER_PORTAL_LOGIN(getCustomerPortalEndpoint("/idp/profile/SAML2/Unsolicited/SSO")),
        INVESTOR_LOGIN(getInvestorEndpoint("/saml/sp/profile/post/acs")),
        INVESTMENTS_OVERVIEW(
                getInvestorEndpoint("/vip/json/0/investors/{" + UrlParameters.DOB + "}")),
        POSITIONS(
                getInvestorEndpoint(
                        "/vip/json/0/positions/csdAccounts/{"
                                + UrlParameters.ACCOUNT_NUMBER
                                + "}")),
        AKSJER_LOGIN(getAksjerEndpoint("/server/rest/auth/login/saml")),
        AKSJER_OVERVIEW(getAksjerEndpoint("/server/rest/me")),
        AKSJER_AVAILABLE_BALANCE(
                getAksjerEndpoint(
                        "/server/rest/customers/{"
                                + UrlParameters.DOB
                                + "}/balance/{"
                                + UrlParameters.CUSTOMER_ID
                                + "}"));

        private final URL requestUrl;

        Url(String requestUrl) {
            this.requestUrl = new URL(requestUrl);
        }

        public static String getNetbankEndpoint(String uri) {
            return UrlParameters.HB_NETBANK_HOST + uri;
        }

        public static String getCustomerPortalEndpoint(String uri) {
            return UrlParameters.CUSTOMER_PORTAL_HOST + uri;
        }

        public static String getInvestorEndpoint(String uri) {
            return UrlParameters.INVESTOR_HOST + uri;
        }

        public static String getAksjerEndpoint(String uri) {
            return UrlParameters.AKSJER_HOST + uri;
        }

        @Override
        public URL get() {
            return this.requestUrl;
        }

        @Override
        public URL parameter(String key, String value) {
            return this.requestUrl.parameter(key, value);
        }

        @Override
        public URL queryParam(String key, String value) {
            return this.requestUrl.queryParam(key, value);
        }

        public URL parameters(Object... params) {
            return new URL(String.format(this.requestUrl.toString(), params));
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UrlParameters {
        static final String HB_NETBANK_HOST = "https://nettbank.handelsbanken.no";
        static final String CUSTOMER_PORTAL_HOST = "https://customerportal.edb.com";
        static final String INVESTOR_HOST = "https://investor.vps.no";
        static final String AKSJER_HOST = "https://aksjer.handelsbanken.no";
        public static final String SESSION_1 = "e1s1";
        public static final String SESSION_2 = "e1s2";
        static final String DOB = "dateOfBirth";
        static final String ACCOUNT_NUMBER = "accountNumber";
        static final String CUSTOMER_ID = "customerId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class QueryParams {
        public static final String SO = "so";
        public static final String DATE = "date";
    }

    public enum QueryParamPairs {
        SHIBBOLETH_ENDPOINT("endpoint", "shibboleth"),
        INVESTOR_PROVIDER_ID("providerId", "https://investor.vps.no:443"),
        AKSJER_PROVIDER_ID("providerId", "https://aksjer.handelsbanken.no/"),
        INVESTOR_TARGET("target", "/vip/auth/sts?vipLandingPage=fund&avtalehaver=09055"),
        KEEP_ALIVE("authenticated", "false");

        private final String key;
        private final String value;

        QueryParamPairs(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Headers implements HeaderEnum {
        ORIGIN("Origin", UrlParameters.HB_NETBANK_HOST),
        REQUEST_WITH("X-Requested-With", "XMLHttpRequest"),
        X_EVRY_CLIENT("X-EVRY-CLIENT-CLIENTNAME", "SMARTbankMobile"),
        // at this moment, any random 8 numeric/alphabets works as requestId, but they might change
        // later
        X_EVRY_CLIENT_REQUESTID("X-EVRY-CLIENT-REQUESTID", "11111111"),
        USER_AGENT(
                "User-Agent",
                String.format(
                        "MB %s 9055 %s %s %s",
                        APP_VERSION,
                        DeviceProfileConfiguration.IOS_STABLE.getModelNumber(),
                        DeviceProfileConfiguration.IOS_STABLE.getOs(),
                        DeviceProfileConfiguration.IOS_STABLE.getOsVersion()));

        private final String key;
        private final String value;

        Headers(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Header {
        static final String EVRY_TOKEN = "X-EVRY-CLIENT-ACCESSTOKEN";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Tags {
        static final String JSESSION_ID = "JSESSIONID";
        public static final String REFERENCE_WORD = "bidm_ref-word";
        public static final String NAME = "name";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String TRANSACTIONS = "transactions";
        static final String NONCE = "SECESB_NONCE";
        static final String SESSION_STAMP = "SECESB_SESSION_STAMP";
        static final String SESSION_STAMP_VALUE = "SECESB_SESSION_STAMP_VALUE";
        public static final String SAML_RESPONSE = "SAMLResponse";
        public static final String LOGIN_ERROR = "bidmob-login-error";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class InitBankIdForm {
        public static final String FORM = "form1";
        public static final String FORM_VALUE = "form1";
        public static final String PHONE_NUMBER = "phonenumber";
        public static final String BIRTHDATE = "birthdate";
        public static final String BTN = "nextBtn";
        public static final String VIEWSTATE = "javax.faces.ViewState";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class BankIdAuthenticationStatus {
        public static final String NONE = "NONE";
        public static final String ERROR = "ERROR";
        public static final String COMPLETE = "COMPLETE";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FinalizeBankIdForm {
        public static final String FORM = "bidmobStep2Form";
        public static final String FORM_VALUE = "bidmobStep2Form";
        public static final String BTN = "completeBtn";
        public static final String VIEWSTATE = "javax.faces.ViewState";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FinalizeInvestorLoginForm {
        public static final String RELAY_STATE = "RelayState";
        public static final String SAML_RESPONSE = "SAMLResponse";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LogInRequestConstants {
        public static final String TOKEN_TYPE = "EvrySO";
        public static final String TOKEN_PROTOCOL_VERSION = "ATP-1.0";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SMSConstants {
        public static final String TYPE = "SAM";
        public static final String LOCALE = "en_SE";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ActivationCodeFieldConstants {
        public static final String DESCRIPTION = "Activation code";
        public static final String NAME = "activationCode";
        public static final Integer LENGTH = 8;
        public static final String PATTERN_ERROR = "The activation code is not valid";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AccountType {
        static final String SAVING = "saving";
        static final String YOUTH_SAVING = "bsu";
        static final String SPENDING = "spending";
        static final String OTHER_ACCOUNT = "other";
        static final String UNKNOWN = "unknown";

        public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
                AccountTypeMapper.builder()
                        .put(AccountTypes.SAVINGS, SAVING, YOUTH_SAVING)
                        .put(AccountTypes.CHECKING, SPENDING, OTHER_ACCOUNT, UNKNOWN)
                        .build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LoanType {
        public static final Map<String, LoanDetails.Type> LOANS_DESCRIPTIONS =
                ImmutableMap.<String, LoanDetails.Type>builder()
                        .put("bolig", LoanDetails.Type.MORTGAGE)
                        .put("år annu ek", LoanDetails.Type.MORTGAGE)
                        .put("Lån annuitet", LoanDetails.Type.MORTGAGE)
                        .build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class InvestmentConstants {
        public static final String STOCK_PORTAL = "stock portal";
        public static final String INVESTOR_PORTAL = "investor";
        public static final String HB_NORWAY = "HB-NORWAY";
        public static final String STATUS_CLOSED = "AVSLUTTET";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Storage {
        public static final String EVRY_TOKEN = "evryToken";
        public static final String ACTIVATE_EVRY_TOKEN = "activateEvryToken";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorCode {
        public static final String WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE_ERROR_CODE = "C161";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EncapConstants {

        static final String ENCAP_API_VERSION = "3.5.4";

        static final String CREDENTIALS_APP_NAME_FOR_EDB = "HANDELSBANKEN_SMBM";

        static final String APP_ID = "com.evry.mobilbanken.handelsbanken";

        static final String RSA_PUB_KEY_STRING =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv0zsNwsaDIgQ/6DKhpVeqdfRf"
                        + "8Xd6Bl+azzeTXA6jA7jzz65FmOWIKWDxj+NJDGvgbqYpawpLus1nYA/OzB9n82CGz/lFgx"
                        + "r//0JbASQP2QnCr19p0EXtwAHI1ctAFW3rxeR/+Y1Ji1Qa5h6pmuWggyC9TGNcrsrk8zRV"
                        + "Z9GBTavkQzDu4oxznfw9ERmWjkaYdGst7ULaH5rpPRuSiOAK2wHjP0yRrK1hSbNsedTCSR"
                        + "jDXl3/ISc12E9RNMKwk4YHFXhy8kqBwTW8rgDAaZdIWuqj650aYOGD4yDI3Fm1+yyIKAEq"
                        + "/f5nf7i+K8ZasjcqJ62nW3MV3cjJ/x2yUM8FwIDAQAB";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final long TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class RetryFilter {
        public static final int NUM_TIMEOUT_RETRIES = 10;
        public static final long RETRY_SLEEP_MILLISECONDS = 10000;
    }
}
