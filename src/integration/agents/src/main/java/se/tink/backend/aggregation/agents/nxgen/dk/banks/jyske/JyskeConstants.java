package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;
import java.security.interfaces.RSAPublicKey;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class JyskeConstants {

    public static final Charset CHARSET = Charsets.UTF_8;

    public static final class Url {
        private static final String BANKDATA_BASE_URL = "https://mobil.bankdata.dk/mobilbank";
        private static final String SERVICE_BASE_URL =
                "https://mobile-services.jyskebank.dk/mobilebank.services/rest";

        public static final URL NEMID_INIT = toBankDataUrl("/nemid/init");

        public static final URL NEMID_GET_CHALLANGE = toBankDataUrl("/nemid/get_challange");

        public static final URL NEMID_ENROLL = toBankDataUrl("/nemid/inroll");
        public static final URL NEMID_LOGIN = toBankDataUrl("/nemid/login_with_installid_prop");
        public static final URL GET_ACCOUNTS = toBankDataUrl("/accounts");

        public static final URL GET_TRANSACTIONS = toBankDataUrl("/pfm/transactions");
        public static final URL GET_FUTURE_TRANSACTIONS = toBankDataUrl("/pfm/transactions/future");
        public static final URL GET_INVESTMENT_GROUPS = toBankDataUrl("/investment/groups");
        public static final URL LOGOUT = toBankDataUrl("/invalidate");
        public static final URL TRANSPORT_KEY = toMobileServiceUrl("/V1-0/transportkey");
        public static final URL MOBILE_SERVICE_LOGIN = toMobileServiceUrl("/V1-0/login");
        public static final URL GET_CARDS = toMobileServiceUrl("/V1-0/cardapp/cards");

        private static URL toBankDataUrl(String endpoint) {
            return new URL(BANKDATA_BASE_URL + endpoint);
        }

        private static URL toMobileServiceUrl(String endpoint) {
            return new URL(SERVICE_BASE_URL + endpoint);
        }
    }

    public static final class Header {
        public static final String APP_ID_KEY = "x-app-id";
        public static final String APP_ID_VALUE = "ios_phone_jyskemobilbank";
        public static final String APPID_KEY = "x-appid";
        public static final String APPID_VALUE = APP_ID_VALUE;
        public static final String VERSION_KEY = "x-version";
        public static final String VERSION_VALUE = "3.10.2";
        public static final String BANKNO_KEY = "x-bankNo";
        public static final String BANKNO_VALUE = "51";
        public static final String OS_KEY = "x-os";
        public static final String OS_VALUE = "ios";

        public static final String BUILDNO_KEY = "x-buildNo";
        public static final String BUILDNO_VALUE = "1364";

        public static final String PERSONALID_KEY = "x-personalId";
    }

    public static final class Crypto {

        public static final String RSA_LABEL = "jbprodver001";
        public static final RSAPublicKey PRODUCT_NEMID_KEY =
                JyskeSecurityHelper.convertToPublicKey(
                        ("-----BEGIN CERTIFICATE-----\n"
                                        + " MIIEVDCCAzygAwIBAgIJAOZ1nSI+Z/1TMA0GCSqGSIb3DQEBBQUAMHkxCzAJBgNVBAYTAkRLMQowCAYDVQQIEwEgMR0wGwYDVQQ"
                                        + "HExRFcnJpdHNvZSwgRnJlZGVyaWNpYTERMA8GA1UEChMIQmFua2RhdGExDDAKBgNVBAsTA1NJSzEeMBwGA1UEAxMVTW9iaWxiYW5"
                                        + "rIHZlcjAwMSBQcm9kMB4XDTE2MDQxMTE1MTE0OFoXDTQxMDQwNTE1MTE0OFoweTELMAkGA1UEBhMCREsxCjAIBgNVBAgTASAxHTA"
                                        + "bBgNVBAcTFEVycml0c29lLCBGcmVkZXJpY2lhMREwDwYDVQQKEwhCYW5rZGF0YTEMMAoGA1UECxMDU0lLMR4wHAYDVQQDExVNb2J"
                                        + "pbGJhbmsgdmVyMDAxIFByb2QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCfhSKIK21UpwaEWhnyDr0SPXdhQc5+qB9"
                                        + "0Y3ioQKBqgnySMlBqULw6AEZ3SXREikzdHInaBlscP97NKvinMv0j2wogJ4lDikDuXidWofNdce2eGdudqO2Cf0uwfxR1Mrl0PRU"
                                        + "UR1gYSvU9HeuoEdLKMxqbewYYbBPOYUWfc5kTRFNfz03ScFp/AZogO/F49or5Pn1U41u29MiTnvHD2PlqfklLScyTA+iULZ+NH3W"
                                        + "9/eGZ6igCpZubUSkj3JVaahJip9OE3usGSet0dQT+ckQ94mOvzzRC/e/0C+faxtXbqwLhnLm1LpOwdkSGLcW/EgmIO5pv1Gf3IX5"
                                        + "HmlLQlIN1AgMBAAGjgd4wgdswHQYDVR0OBBYEFH/N+XATSI+6MdtD5ZbDk/3ev9PEMIGrBgNVHSMEgaMwgaCAFH/N+XATSI+6Mdt"
                                        + "D5ZbDk/3ev9PEoX2kezB5MQswCQYDVQQGEwJESzEKMAgGA1UECBMBIDEdMBsGA1UEBxMURXJyaXRzb2UsIEZyZWRlcmljaWExETA"
                                        + "PBgNVBAoTCEJhbmtkYXRhMQwwCgYDVQQLEwNTSUsxHjAcBgNVBAMTFU1vYmlsYmFuayB2ZXIwMDEgUHJvZIIJAOZ1nSI+Z/1TMAw"
                                        + "GA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBADqGsF7mxb1CxpRaAOv5+w5+B44rQykqZP3iic14A/zG+6mHLleyscwIiYQ"
                                        + "lW9iw480sac0Zt7GoxiWZChFa+PD/+joLB/6lFO+/bx0GPWy5SkPdZII7689/Zo70rJdyZRd1tp6qk/O/+WDPy+M0w90HiNZG0q0"
                                        + "NqxfomZvZ6fAFuPVsupJIJq6DLL24FvBcCiJm5v6LPjjr9/lKwXZj5pBR9icqBucHhPmrv9YQmMwEyHnDqK27NKKim0Xny8w7xGW"
                                        + "V5It/uTbJMNetYXIiWkW0wKZkS71hgPSSkfMWhN+tx5eViVG7VVI/CN4Aj6zrfPW6moETruOfyOQmmq1lr1A=\n"
                                        + "-----END CERTIFICATE-----")
                                .getBytes(JyskeConstants.CHARSET));

        public static final String AES_PADDING = "XOXOXOXOXOXOXOXO";
        public static final String CERT_TYPE = "X.509";
        public static final RSAPublicKey MOBILE_SERVICE_KEY =
                RSA.getPubKeyFromBytes(
                        Base64.decodeBase64(
                                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCoahwTZuZhJxmSDdOQeTIfNpohuipfQS4ttaypknG7lOX4Y"
                                        + "+bQf8xYpQx6C2fs4hR+W6KupEJBuxb2a8ENoEbxnXsbLv9YtDTmNQbNJ14ED"
                                        + "+iRXqzgatmMtZs3RxQiK2qJ0InVjTSnNwHS3WAOhplp74TeCiEpoAsAlyzT7VPUEQIDAQAB"));
    }

    public static final class Storage {
        public static final String INSTALL_ID = "installId";
    }

    public static final class ErrorCode {
        public static final int INVALID_CREDENTIAL = 112;
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.SAVINGS, "Jyske Munnypot", "Opsparing")
                    .put(
                            AccountTypes.CHECKING,
                            "Budget",
                            "Totalkonto",
                            "Totalkonto Ung",
                            "Lønkonto",
                            "Budgetkonto",
                            "Budgetkonto Ung",
                            "Grundkonto",
                            "Forbrug")
                    .build();

    public static final class Log {
        public static final LogTag CREDITCARD_LOGGING = LogTag.from("#dk_jyske_creditcard");
        public static final LogTag INVESTMENT_LOGGING = LogTag.from("#dk_jyske_investment");
    }

    public static final class Fetcher {

        public static final class CreditCard {
            public static final String DANKORT = "DANKORT";
            public static final String DEBIT = "DEBIT";
        }

        public static final class Investment {
            public static final String PENSION_TYPE = "pension";
            public static final String CHILD_SAVING_TYPE = "childsaving";
            public static final String CURRENCY = "DKK";
        }
    }
}
