package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import se.tink.backend.aggregation.nxgen.http.HeaderEnum;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;

public class HandelsbankenNOConstants {

    public static final int AUTHENTICATION_TIMEOUT_COUNT = 15;  // set to 15 currently same as SparebankenSor

    public enum Url implements UrlEnum {
        APP_INFORMATION("/smbmobile/9055/appversion_ios.json"),
        VERIFY_CUSTOMER("/secesb/rest/era/era/public/customers/%s/mobile?number=+47%s&orgid=9055"),
        CONFIGURE_BANKID("/authenticate/login/bankidmobile?configKey=smbmactivate9055&userid=%s&phoneNumber=%s"),
        BANKID_1("/authenticate/login/bankidmobile;jsessionid=%s?execution=e1s1"),
        BANKID_2("/authenticate/login/bankidmobile;jsessionid=%s?execution=e1s2"),
        POLL_BANK("/authenticate/login/rest/bankidmobilestatus.json;jsessionid=%s"),
        LOGIN_FIRST_STEP("/secesb/rest/esb/v1/login"),
        LOGIN_SECOND_STEP("/secesb/rest/era/login"),
        SEND_SMS("/secesb/rest/era/sam/sms"),
        ACCOUNTS("/secesb/rest/era/accounts"),
        TRANSACTIONS("/secesb/rest/era%s?number=%s&include_authorizations=true&index=%s"),
        KEEP_ALIVE("/secesb/rest/esb/v1/keepalive");

        private URL url;

        Url(String uri) {
            this.url = new URL(UrlParameters.HOST + uri);
        }

        @Override
        public URL get() {
            return this.url;
        }

        @Override
        public URL parameter(String key, String value) {
            return this.url.parameter(key, value);
        }

        @Override
        public URL queryParam(String key, String value) {
            return this.url.queryParam(key, value);
        }

        public URL parameters(Object... params) {
            return new URL(String.format(this.url.toString(), params));
        }
    }

    public static final class UrlParameters {
        public static final String HOST = "https://nettbank.handelsbanken.no";
        public static final String USER_ID = "userId";
        public static final String PHONE_NUMBER = "phoneNumber";
        public static final String SESSION_1 = "e1s1";
        public static final String SESSION_2 = "e1s2";
    }

    public enum Headers implements HeaderEnum {
        ORIGIN("Origin", UrlParameters.HOST),
        REQUEST_WITH("X-Requested-With", "XMLHttpRequest"),
        X_EVRY_CLIENT("X-EVRY-CLIENT-CLIENTNAME", "SMARTbankMobile"),
        USER_AGENT("User-Agent", "MB 1.20.1 9055 iPhone 6s iOS 10.2");
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

    public static final class Header {
        public static final String EVRY_TOKEN = "X-EVRY-CLIENT-ACCESSTOKEN";
        public static final String REQUEST_ID = "X-EVRY-CLIENT-REQUESTID";
    }

    public static final class Tags {
        public static final String JSESSION_ID = "JSESSIONID";
        public static final String REFERENCE_WORD = "bidm_ref-word";
        public static final String EVRY_TOKEN_FIELD_KEY = "name";
        public static final String EVRY_TOKEN_FIELD_VALUE = "so";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String TRANSACTIONS = "transactions";
        public static final String NONCE = "SECESB_NONCE";
        public static final String SESSION_STAMP = "SECESB_SESSION_STAMP";
        public static final String SESSION_STAMP_VALUE = "SECESB_SESSION_STAMP_VALUE";
    }

    public static final class InitBankIdForm {
        public static final String FORM = "form1";
        public static final String FORM_VALUE = "form1";
        public static final String PHONE_NUMBER = "phonenumber";
        public static final String BIRTHDATE = "birthdate";
        public static final String BTN = "nextBtn";
        public static final String VIEWSTATE = "javax.faces.ViewState";
    }

    public static final class BankIdAuthenticationStatus {
        public static final String NONE = "NONE";
        public static final String ERROR = "ERROR";
        public static final String COMPLETE = "COMPLETE";
    }

    public static final class FinalizeBankIdForm {
        public static final String FORM = "bidmobStep2Form";
        public static final String FORM_VALUE = "bidmobStep2Form";
        public static final String BTN = "completeBtn";
        public static final String VIEWSTATE = "javax.faces.ViewState";
    }

    public static final class LogInRequestConstants {
        public static final String TOKEN_TYPE = "EvrySO";
        public static final String TOKEN_PROTOCOL_VERSION = "ATP-1.0";
    }

    public static final class SMSConstants {
        public static final String TYPE = "SAM";
        public static final String LOCALE = "en_SE";
    }

    public static final class ActivationCodeFieldConstants {
        public static final String DESCRIPTION = "Activation code";
        public static final String NAME = "activationCode";
        public static final Integer LENGTH = 8;
        public static final String PATTERN_ERROR = "The activation code is not valid";
    }

    public static final class AccountType {
        public static final String SAVING = "saving";
        public static final String YOUTH_SAVING = "bsu";
        public static final String SPENDING = "spending";
    }

    public static final class EncapConstants {

        public static final String applicationVersion = "11402";

        public static final String encapApiVersion = "3.3.5";

        public static final String credentialsAppNameForEdb = "HANDELSBANKEN_SMBM";

        public static final String credentialsBankCodeForEdb = "(null)";

        public static final String saIdentifier = "samobile_hb_mobile_ios_v1";

        public static final String appId = "com.evry.mobilebanken.handelsbanken";

        public static final String suffixForDeviceHash = "ijljlsfss";

        public static final String
                rsaPubKeyString =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3maOiHUOhZR75rlXiyic"
                        + "csi5mp5OEdkamnC1oRO1o71eP2u7v3i3sEIHQ9jHaIw6kHCrHqFCPvgjvbzcM8vC"
                        + "uHZF3xafYCxShUH6Kb5AU7of6L7dTqXJDwyK6EJ1sGX1qIrlqVdYzDtfEES7NZb4"
                        + "nJOpcFzeG9Nt9N7slm4Xq7KFYHFSkVXOWF2Se9f/raoaYVkFCNK8XClw1wPRnkc0"
                        + "587xE1qwUa661m/pmCkm6M0FO7wfdS9zOQuq9Ual1x2sD7q+H2UhKtmY9zb31paM"
                        + "ZDa6Tr3/eHopfisV/g1LxeVx/99tVf7b3vdAbBlcBep6YaawnhWM27NGEZ/jldzK"
                        + "YQIDAQAB";

        public static final String clientPrivateKeyString =
                "MIICXQIBAAKBgQC4gh8oXob7B0mrdW1EOV4BMHuloZHnrQ9p3dLJHX6YKeYwAXEx"
                        + "+AopG5+X27PBlasY7c4lm1VrYtbhgSiA5bvfBDQ5/OAMqTJ6o9VBMgU0TuXYc9C+"
                        + "jUmZf04flu1T2GEqpU3jISCubbfiH7ksJQNtdqqq6AW8W+rKGNgBn5Rn2QIDAQAB"
                        + "AoGBAJPBEDaBzFJGecZmmQPjdNY6/ymag38l5Yv/6YbIqdIs63tSDFXZrjlRN7Ki"
                        + "bAcAVi32cflHtVXzpuSbS9Y9Iv6MbNpNJi1KAEe+hYZs2iuyi7Bjotom+T0tyjLU"
                        + "QnYwsrhPvpkQ7p01MKpGTcbvXGxelcDs1yaYcqGlHdL486ZpAkEA9ZS/KlzRKRyr"
                        + "EbWrBsX1qV/5MPEDQnCCVQo6bNifHXHgJutmhTYe/wJPvljs2URDvwB8irbWEkuc"
                        + "Hk04G5ZbLwJBAMBWEHwiErD2pvuqFYIyacrrbLZ5JrQp/nVlqB7dgYHKf0oW7zi7"
                        + "OY3DKgTGPMD8Ar+G/B6eFuv9d96psWBLC3cCQEsD890J2y5Wvn67YpHAGIlzcpgb"
                        + "luZNndJCPJSRGxGQfmsFDxzz6kX1O8ymNzsq2hLXIDPzI7MU+4xaBCCRLisCQAzK"
                        + "LFJ02ZVW8Yeuqbt8qrhJq3L+32n/mOIpnmJfDGubEZfVqpa1LICWx8aFgCi9GVmv"
                        + "GjjW/mM2+a+ezSLAGsUCQQCQziZRLYLf0vRiITGNwEvj6qxt7YHmzc1tUjYkgYB5"
                        + "fqtRp234QkKHCujiBh2t9g3ObvOtEuEzxYOwupLE3tYK";
    }
}
