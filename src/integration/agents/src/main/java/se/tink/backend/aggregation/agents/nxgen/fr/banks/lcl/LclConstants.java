package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class LclConstants {

    public static final class Urls {
        private static final String HOST = "https://sdk-lcl-prod.apnl.ws/";
        private static final String SECURITY_HOST = "https://mobile.particuliers.secure.lcl.fr/";

        public static final URL DEVICE_CONFIGURATION = new URL(HOST + "v4/sdk/devices");
        public static final URL SET_IDENTITY =
                new URL(SECURITY_HOST + "outil/UAUT/AccueilService/setIdentSC");
        public static final URL LOGIN = new URL(SECURITY_HOST + "outil/UAUT/AccueilService/login");
        public static final URL KEEP_ALIVE =
                new URL(SECURITY_HOST + "outil/UWOA/api/v2/auth/token");

        public static final URL ACCOUNT_DETAILS_LIST =
                new URL(SECURITY_HOST + "outil/UWRI/AccueilService");
        public static final URL ACCOUNT_DETAILS =
                new URL(SECURITY_HOST + "outil/UWRI/AccueilService/detailRib");
        public static final URL ACCESS_SUMMARY =
                new URL(SECURITY_HOST + "outil/UWSP/Synthese/accesSynthese");
        public static final URL TRANSACTIONS =
                new URL(SECURITY_HOST + "outil/UWLM/ListeMouvementsPar/accesListeMouvementsPar");
    }

    public static final class Storage {
        public static final String DEVICE_ID = "deviceId";
        public static final String AGENT_KEY = "agentKey";
        public static final String ACCOUNT_DETAILS_ENTITY = "accountDetailsEntity";
    }

    public static final class DeviceConfiguration {
        public static final String USER_AGENT = "User-Agent";
        public static final String MODEL_VERSION = "iPhone8,1";
        public static final String APP_NAME = "lcl-prod";
        public static final String APP_VERSION = "3.11.3";
        public static final String SDK_VERSION = "4.6.3";
        public static final String OS = "iOS";
        public static final String FUSEAU = "7200";
        public static final String CONNECTION_TYPE = "wifi";
        public static final String MANUFACTURER = "Apple";
        public static final String LANG = "en";
        public static final String TIMEZONE = "Europe";
        public static final String OS_VERSION = "10.2";
        public static final String MODEL = "iPhone";
        public static final String NAME = "Tink";
    }

    public static final class Headers {
        public static final String X_AP_DEVICEUUID = "X-AP-DeviceUID";
        public static final String X_AP_REALTIME = "X-AP-RealTime";
    }

    public enum HeaderValuePairs {
        X_AP_NETWORK("X-AP-Network", DeviceConfiguration.CONNECTION_TYPE),
        X_AP_SCREEN("X-AP-Screen", "375*667"),
        X_AP_OS("X-AP-OS", DeviceConfiguration.OS),
        X_AP_SDK_VERSION("X-AP-SDKVersion", DeviceConfiguration.SDK_VERSION),
        X_AP_APP_VERSION("X-AP-AppVersion", DeviceConfiguration.APP_VERSION),
        X_AP_KEY("X-AP-Key", "sygU9znL");

        private final String key;
        private final String value;

        HeaderValuePairs(String key, String value) {
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

    public static final class Authentication {
        public static final String IDENTIFIER_FOR_VENDOR_IN_HEX = "C6F24A952801";
        public static final String CHANNEL = "MOBILE";

        public static final String CODE_ID_XOR = "CodeIdXor";
        public static final String LCL_BPI_METADATA = "LCL-BPI-Metadata";
        public static final String IDENTIFIANT = "identifiant";
        public static final String ERROR_TRUE = "true";
        public static final String INCORRECT_PASSWORD = "UAUT100";
        public static final String INCORRECT_LOGIN_CREDENTIALS = "UAUT200";
    }

    public enum AuthenticationValuePairs {
        DEVICE("device", "04"),
        IDENTIFIANT_ROUTING("identifiantRouting", "CLI"),
        MOBILE("mobile", "true"),
        AUDIENCE("audience", "MONETIQUE");

        private final String key;
        private final String value;

        AuthenticationValuePairs(String key, String value) {
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

    public static final class AccountTypes {
        public static final String CHECKING_ACCOUNT_TAG = "familleDepot";
        public static final String CHECKING_ACCOUNT = "Compte de dépôts";
    }

    public static final class AccountDetailsRequest {
        public static final String ACCOUNT = "compte";
    }

    public static final class TransactionsRequest {
        public static final String AGENCY = "agence";
        public static final String ACCOUNT_NUMBER = "compte";
        public static final String CLE_LETTER = "lettreCle";
        public static final String MODE_KEY = "mode";
        public static final String MODE_VALUE = "45";
        public static final String TYPE_KEY = "nature";
        public static final String TYPE_VALUE = "006";
        public static final String TAG_HTML_KEY = "tagHtml";
        public static final String TAG_HTML_VALUE = "CD";
    }

    public static final class TransactionDescrFormatting {
        public static final String DATE_PATTERN = "dd/MM/yyyy";
        public static final String MERCHANT_NAME = "merchantName";
        public static final String REGEX =
                String.format(
                        "^CB\\s*(?<%s>.*)\\s*([0-9]{2}\\/[0-9]{2}\\/[0-9]{2})+$", MERCHANT_NAME);
    }

    public static final class Logs {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("lcl_unknown_account_type");
    }
}
