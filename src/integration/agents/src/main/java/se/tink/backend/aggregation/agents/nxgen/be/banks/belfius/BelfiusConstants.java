package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BelfiusConstants {

    public static final ImmutableMap<String, String> HEADERS =
            ImmutableMap.<String, String>builder()
                    .put("User-Agent", "I/PHONE/BDM/09310/PRD")
                    .put("Connection", "keep-alive")
                    .put("Accept-Encoding", "br, gzip, deflate")
                    .put("Accept-Language", "nl-be")
                    .build();
    public static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES =
            ImmutableMap.<String, AccountTypes>builder()
                    .put("A0", AccountTypes.CHECKING)
                    .put("B0", AccountTypes.SAVINGS)
                    .put("H0", AccountTypes.PENSION)
                    .put("F0", AccountTypes.CREDIT_CARD)
                    .build();
    public static final String TRANSACTION_BANK_NAME = "Belfius Belgium";
    public static final int MAX_NUMBER_OF_TRANSACTION_PAGES = 100;
    public static final int FIRST_TRANSACTION_PAGE = 0;

    public static final String TINK_FRENCH = "fr";
    public static final String BRAND = "Apple iPhone 7";

    public enum ExecutionMode {
        AGGREGATED,
        SERVICES,
    }

    public static class UrlParameter {
        public static final String MACHINE_IDENTIFIER = "machineIdentifier";
    }

    public static final class Url {
        private static final String BASE = "https://m.belfius.be/";

        public static final URL GEPA_RENDERING_URL =
                new URL(
                        BASE
                                + "F2CRenderingMobile/GEPARendering/machineIdentifier={"
                                + UrlParameter.MACHINE_IDENTIFIER
                                + "}");

        public static final URL GEPA_SERVICE_URL =
                new URL(
                        BASE
                                + "F2CRenderingMobile/GEPAService/machineIdentifier={"
                                + UrlParameter.MACHINE_IDENTIFIER
                                + "}");

        static final URL CONFIG_IOS = new URL(BASE + "configIOS.json");
    }

    public static class Storage {
        public static final String SESSION_ID = "SESSION_ID";
        public static final String MACHINE_IDENTIFIER = "MACHINE_IDENTIFIER";
        public static final String REQUEST_COUNTER_AGG = "REQUEST_COUNTER";
        public static final String REQUEST_COUNTER_SVC = "REQUEST_COUNTER_SERVICES";
        public static final String DEVICE_TOKEN = "DEVICE_TOKEN";
    }

    public static final class Widget {
        public static final String VERSION_KIND_APP =
                "Container@reuse_LogonSoft@minp_VersionKindApp";
        public static final String DEVICE_TOKEN_HASHED =
                "Container@reuse_LogonSoft@inp_deviceTokenHashed";
        public static final String DEVICE_TOKEN_HASHED_IOS_COMPARISON =
                "Container@reuse_LogonSoft@inp_deviceTokenHashedIosComparison";
        public static final String SIGNATURE = "Container@reuse_LogonSoft@inp_signature";
        public static final String IS_GUEST = "Container@reuse_LogonSoft@minp_IsGuest";
        public static final String TYPE_LOGON_DEVICE =
                "Container@reuse_LogonSoft@minp_TypeLogonDevice";
        public static final String CODE_SDK = "Container@reuse_LogonSoft@minp_CodeSDK";
        public static final String DEV_T = "Container@reuse_LogonSoft@minp_dev_t";
        public static final String ROOT = "Container@reuse_LogonSoft@minp_root";
        public static final String ROOT_H = "Container@reuse_LogonSoft@minp_root_h";
        public static final String EMUL = "Container@reuse_LogonSoft@minp_emul";
        public static final String DEBUG = "Container@reuse_LogonSoft@minp_debug";
        public static final String TAMPER = "Container@reuse_LogonSoft@minp_tamper";
        public static final String NS_WIFI = "Container@reuse_LogonSoft@minp_ns_wifi";
        public static final String UNKW_S = "Container@reuse_LogonSoft@minp_unkw_s";
        public static final String HOST = "Container@reuse_LogonSoft@minp_host";
        public static final String MALW = "Container@reuse_LogonSoft@minp_malw";
        public static final String MALW_L = "Container@reuse_LogonSoft@minp_malw_l";
        public static final String SMS_LIS = "Container@reuse_LogonSoft@minp_sms_lis";
        public static final String FACE_D = "Container@reuse_LogonSoft@minp_face_d";
        public static final String SYS_VER = "Container@reuse_LogonSoft@minp_sys_ver";
        public static final String APP_VER = "Container@reuse_LogonSoft@minp_app_ver";
        public static final String AUTHENTICATE = "Container@reuse_LogonSoft@btn_authenticate";

        // AuthenticateWithCodeRequest
        public static final String IWS_LOGIN_AUTHENTIFICATION =
                "Container@reuse_IWSLogin@btn_Authentification";
        public static final String IWS_LOGIN_SIGNATURE = "Container@reuse_IWSLogin@inp_signature";

        // PrepareAuthenticationRequest
        public static final String SECURITY_TYPE = "Container@b_SecurityType";
        public static final String PAN = "Container@inp_Pan";
        public static final String LOGIN_IWS = "Container@b_LoginIWS";
        public static final String IWS_LOGIN_PREPARE_AUTHENTIFICATION =
                "Container@reuse_IWSLogin@btn_Prepare_Authentification";
        public static final String IWS_LOGIN_SIGNATURE_CHALLENGE =
                "Container@reuse_IWSLogin@reuse_signature@lb_Challenge";

        // PrepareDeviceRegistrationRequest
        public static final String DEVICE_REGISTRATION = "Container@b_DeviceRegistation";
        public static final String DEVICE_REGISTRATION_PREPARE_REGISTRATION =
                "Container@reuse_DeviceRegistrationGeneric@btn_PrepareRegistration";
        public static final String DEVICE_REGISTRATION_DEV_TOKEN =
                "Container@reuse_DeviceRegistrationGeneric@inp_dev_token";
        public static final String DEVICE_REGISTRATION_BRAND =
                "Container@reuse_DeviceRegistrationGeneric@inp_brand";
        public static final String DEVICE_REGISTRATION_MODEL =
                "Container@reuse_DeviceRegistrationGeneric@inp_model";
        public static final String DEVICE_REGISTRATION_SIGNATURE_CHALLENGE =
                "Container@reuse_DeviceRegistrationGeneric@reuse_signature@lb_Challenge";

        public static final String LOGON_SOFT = "Container@mb_LogonSoft";
        public static final String LOGIN_SOFT_CHALLENGE = "Container@reuse_LogonSoft@lbl_challenge";
        public static final String LOGON_SOFT_GET_CHALLENGE =
                "Container@reuse_LogonSoft@btn_getChallenge";
        public static final String LOGON_SOFT_CONTRACT_NUMBER =
                "Container@reuse_LogonSoft@lb_contract_nr";

        // RegisterDeviceRequest
        public static final String DEVICE_REGISTRATION_REGISTER =
                "Container@reuse_DeviceRegistrationGeneric@btn_Register";
        public static final String DEVICE_REGISTRATION_SIGNATURE =
                "Container@reuse_DeviceRegistrationGeneric@inp_signature";

        // FetchProductsRequest
        public static final String PRODUCT_LIST_LOAD = "Container@b_ProductList_Load";
        public static final String PRODUCT_LIST_REPEATER_DETAIL =
                "Container@reuse_ProductList@repeater_detail";

        // FetchTransactionsRequest
        public static final String HISTORY_SEARCH = "Container@b_History_Search";
        public static final String HISTORY_FIND = "Container@reuse_History@btn_Find";
        public static final String HISTORY_TYPE_TRANSACTIONS =
                "Container@reuse_History@inp_type_transactions";
        public static final String HISTORY_CURRENCY = "Container@reuse_History@inp_Currency";
        public static final String HISTORY_HIST = "Container@reuse_History@rep_hist";
        public static final String HISTORY_HAS_NEXT = "Container@reuse_History@lb_hasNext";
        public static final String HISTORY_BTN_NEXT = "Container@reuse_History@btn_Next";

        // FetchUpcomingTransactionsRequest
        public static final String UPCOMING_TRANSACTIONS = "Container@reuse_Pending@rep_pending";
        public static final String UPCOMING_TRANSACTIONS_BUTTON_PENDING = "Container@b_Pending";
        public static final String UPCOMING_TRANSACTIONS_BUTTON_FIND =
                "Container@reuse_Pending@btn_Find";
        public static final String UPCOMING_TRANSACTIONS_HAS_NEXT =
                "Container@reuse_Pending@lb_hasNext";
        public static final String UPCOMING_TRANSACTIONS_BTN_NEXT =
                "Container@reuse_Pending@btn_Next";

        // Payments
        public static final String TRANSFER_INIT = "Container@b_Transfer";
        public static final String TRANSFER_PAY = "Container@reuse_transfer@btn_pay";
        public static final String ACCOUNT_INPUT = "Container@reuse_transfer@inp_orderingacc";
        public static final String ACCOUNT_OWNER = "Container@reuse_transfer@inp_benefacc";
        public static final String TO_OWN_ACCOUNT = "Container@reuse_transfer@inp_ownbenefaccount";
        public static final String RECIPIENT_NAME = "Container@reuse_transfer@inp_benefname";
        public static final String RECIPIENT_COUNTRY = "Container@reuse_transfer@inp_country";
        public static final String RECIPIENT_MESSAGE = "Container@reuse_transfer@inp_communication";
        public static final String USE_STRUCTURED_MESSAGE =
                "Container@reuse_transfer@inp_structuredCommFlag";
        public static final String TRANSFER_AMOUNT = "Container@reuse_transfer@inp_amount";
        public static final String TRANSFER_DATE = "Container@reuse_transfer@inp_memodate";
        public static final String SHA1_CLIENT = "Container@reuse_transfer@inp_sha1_client";

        // Beneficiary
        public static final String BENEFICIARY_MANAGEMENT = "Container@mb_BeneficiaryManagement";
        public static final String PREPARE_UPGRADE =
                "Container@reuse_BeneficiaryManagement@mb_PrepareUpgrade";
        public static final String LIST_NUMBER =
                "Container@reuse_BeneficiaryManagement@minp_Key1_ListNumber";
        public static final String SEQUENCE_NUMBER =
                "Container@reuse_BeneficiaryManagement@minp_Key2_SequenceNumber";
        public static final String INP_ALIAS = "Container@reuse_BeneficiaryManagement@minp_Alias";
        public static final String INP_BIC = "Container@reuse_BeneficiaryManagement@minp_Bic";
        public static final String INP_EXT_ACC =
                "Container@reuse_BeneficiaryManagement@minp_Ext_Int_Account";
        public static final String INP_AMOUNT = "Container@reuse_BeneficiaryManagement@minp_Amount";
        public static final String INP_NAME =
                "ContaineownBeneficiariesForAccountr@reuse_BeneficiaryManagement@minp_Name";
        public static final String INP_STREET = "Container@reuse_BeneficiaryManagement@minp_Street";
        public static final String INP_HOUSE_NUMBER =
                "Container@reuse_BeneficiaryManagement@minp_HouseNumber";
        public static final String INP_ZIP = "Container@reuse_BeneficiaryManagement@minp_ZipCode";
        public static final String INP_CITY = "Container@reuse_BeneficiaryManagement@minp_City";
        public static final String INP_COUNTRY_CODE =
                "Container@reuse_BeneficiaryManagement@minp_CodeCountry";
        public static final String INP_STRUCTURED_COMMUNICATION =
                "Container@reuse_BeneficiaryManagement@minp_StructuredCommunication";
        public static final String INP_COMMUNICATION =
                "Container@reuse_BeneficiaryManagement@minp_Communication";
        public static final String INP_CONTACT =
                "Container@reuse_BeneficiaryManagement@minp_Contact";
        public static final String GET_BENEFICIARIES =
                "Container@reuse_BeneficiaryManagement@mb_getBeneficiaries";

        // AppRules
        public static final String APP_RULES = "Container@b_applrules";

        // DocumentSign
        public static final String DOCUMENT_SIGN = "Container@mb_DocumentToSign";
        public static final String NUMBER_OF_BUNDLES =
                "Container@reuse_MCSS_DocumentToSign@mb_getNumberOfBundles";

        // DoublePaymentRequest
        public static final String PAY_DOUBLE_BTN = "Container@reuse_transfer@btn_pay_double";

        // EntityClick
        public static final String ENTITY_CLICK = "Container@b_Entity";

        // EntitySelect
        public static final String ENTITY_SWITCHER = "Container@reuse_EntitySwitcher@rep_entity";

        // GetSigningProtocolRequest
        public static final String GET_SIGNING_PROTOCOL =
                "Container@reuse_transfer@mb_GetSigningProtocol";

        // Load Messages
        public static final String MESSAGES = "Container@mb_Messages";
        public static final String NUMBER_OF_MESSAGES =
                "Container@reuse_Messages@mb_GetNumberOfMessages";

        // MenuAccess
        public static final String MENU_ACCESS = "Container@mb_MenuAccess";

        // PrepareReaderPayment
        public static final String PREPARE_READER_PAYMENT =
                "Container@reuse_transfer@btn_PreparePaymentWithReader";

        // SignBeneficiaryRequest
        public static final String UPGRADE_BENEFICIARIES =
                "Container@reuse_BeneficiaryManagement@mb_Upgrade";
        public static final String BENEFICIARY_SIGNATURE =
                "Container@reuse_BeneficiaryManagement@minp_Signature";

        // SignCounters
        public static final String SIGN_COUNTERS = "Container@mb_ToSignCounters";
        public static final String ALL_TO_SIGN =
                "Container@reuse_ToSignCounter@mb_Refresh_AllToSign";

        // SignedPaymentResponse
        public static final String SIGN_PAYMENT_BUTTON =
                "Container@reuse_transfer@btn_Sign_Payment";
        public static final String TRANSFER_SIGNATURE = "Container@reuse_transfer@inp_signature";

        public static final String TRANSFER_SIGN_OK =
                "Container@reuse_transfer@lb_exec_ok_Transfer_Signed";

        // DoubleSign
        public static final String DOUBLE_SIGN_PAYMENT =
                "Container@reuse_transfer@btn_pay_double_signed";
        public static final String DOUBLE_CONFIRM_CLICK =
                "Container@reuse_transfer@mb_DoubleConfirmClicked";
    }

    public static final class Request {
        public static final String VERSION_KIND_APP = "PRD";
        public static final String APPLICATION_ID = "BDM";
        public static final String AGGREGATED_EXECUTION_MODE = "aggregated";

        public static final String APPLICATION_TYPE = "native";
        public static final String LOCALE_DUTCH = "nl_BE";
        public static final String LOCALE_FRENCH = "fr_BE";
        public static final String APP_RELEASE = "09310";
        public static final String PLATFORM = "I";
        public static final String TYPE_DEVICE = "PHONE";

        public static final String SYS_VER = "12.04.000";
        public static final String APP_VER = "09.03.001";

        public static final String CHECK_STATUS_APPLICATION_ID = "services";
        public static final String CHECK_STATUS_EXECUTION_MODE = "sequential";
        public static final String CHECK_STATUS_METHOD_ID = "CheckStatus";
        public static final String CHECK_STATUS_SERVICE_NAME =
                "gef0.gef1.gemd.Contract.diamlservice";

        public static final String START_FLOW_SERVICE_NAME =
                "gef0.gef1.gemd.Native_MobileFlow.diamlflow";
        public static final String GET_APP_MESSAGE_TEXT_NAME =
                "gef0.gef1.gemd.GetAppMessageText.diamlservice";

        public static class Session {
            public static class Attribute {
                public static final String APP_RELEASE = "AppRelease";
                public static final String TYPE_DEVICE = "TypeDevice";
                public static final String PLATFORM = "Platform";
                public static final String VERSION_KIND_APP = "VersionKindApp";
                public static final String APPLICATION = "Application";
            }
        }
    }

    public static class Response {
        public static final String SESSION_OPENED = "SessionOpened";
        public static final String SCREEN_UPDATE = "ScreenUpdate";
        public static final String EXECUTE_METHOD_RESPONSE = "ExecuteMethodResponse";
        public static final String MESSAGE_RESPONSE = "MessageResponse";
        public static final String TECHNICAL_RESPONSE = "TechnicalResponse";
        public static final String RESPONSE_CHALLENGE = "lb_Challenge";
        public static final String REUSE_SIGNATURE = "reuse_signature@rep_scenarios";
        public static final String CARD_READER_ALLOWED = "mlb_CardReaderAllowed";
        public static final String BENEFICIARY_WIDGET =
                "Container@reuse_BeneficiaryManagement@rep_BenefiariesContacts";

        // TechnicalResponse
        public static final String TYPE_HEARTBEAT = "heartbeat";

        public static class Attribute {
            public static final String OPEN_SESSION = "OpenSession";
            public static final String WIDGET_EVENTS = "WidgetEvents";
            public static final String START_FLOW = "StartFlow";
            public static final String EXECUTE_METHOD = "ExecuteMethod";
        }
    }

    public static class Fetcher {
        public static class CreditCards {
            public static final LogTag LOGTAG = LogTag.from("#be_belfius_creditcard");
        }
    }

    public static class ErrorCodes {
        public static final String FATAL_MESSAGE_TYPE = "fatal";
        public static final String SESSION_EXPIRED = "SESSION DOESN'T EXIST";
        public static final String UNKNOWN_SESSION = "UNKNOWN SESSION";
        public static final String ERROR_MESSAGE_TYPE = "error";
        public static final String WRONG_CREDENTIALS_CODE = "GE9KT60O/90AC/1307";
        public static final String ACCOUNT_BLOCKED = "GE9KT60O/90AA/1204";
        public static final String ERROR_SIGN_CODE = "90AC/1300";
        public static final String ERROR_EMPTY_SIGN_CODE = "GE9KT58O/90AA/";
        public static final String MISSING_MOBILEBANKING_SUBSCRIPTION = "GE9KT082/1501/1501";
        public static final String DEVICE_REGISTRATION_ERROR = "GE9KT048/1520/1520";
        public static final String SIGN_TEMP_ERROR_CODE = "12DB/000000";
        public static final String WEEKLY_READER_LIMIT_CODE = "1000/AMW036";
        public static final String BENEFICIARY_LIMIT = "1000/NOBENE";
        public static final String DAILY_LIMIT = "1000/AMD063";
        public static final String WEEKLY_LIMIT = "1208/000000";
        public static final String BENEFICIARY_WEEKLY_LIMIT = "1000/AMW033";
        public static final String INVBALID_SIGN_BENEFICIARY_CODE = "90AC/000000";
    }

    public static class Transfer {
        public static final LogTag LOGTAG = LogTag.from("#be_belfius_transfer");
    }

    static class HttpClient {
        public static final int MAX_RETRIES = 4;
        public static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
