package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

public class BnpParibasConstants {

    public static final class Urls {
        static final String NUMPAD = "ident-mobile-wspl/rpc/grille.do";
        static final String LOGIN = "SEEA-pa01/devServer/seeaserver";
        static final String KEEP_ALIVE = "authentforte-wspl/rpc/recupererStatut";
        static final String LIST_ACCOUNTS = "udc-wspl/rest/getlstcpt";
        static final String TRANSACTIONAL_ACCOUNT_TRANSACTIONS = "rop-wspl/rest/releveOp";
        static final String LIST_IBANS = "virement-wspl/rest/initialisationVirement";
    }

    public static final class Storage {
        public static final String IDFA_UUID = "idfa";
        public static final String IDFV_UUID = "idfv";
        public static final String LOGIN_ID = "loginId";
        public static final String IBAN_KEY = "ibanKey";
    }

    public static final class Errors {
        public static final String INCORRECT_CREDENTIALS = "201";
        public static final String LOGIN_ERROR = "1002";
        public static final String ACCOUNT_ERROR = "21501";
    }

    public static final class Auth {
        // This has to be the user agent for transaction fetching to work
        public static final int NUMPAD_SIZE = 10;
        public static final String INDEX_0 = "0";

        public static final String GRID_TYPE = "typeGrille";
        public static final String AUTH = "AUTH";
    }

    public static final class AuthFormValues {
        public static final String MEAN_ID = "MOBILEWEBSEAL";
        public static final String ID_GRILLE = "idGrille";
        public static final String POS_SELECT = "posSelect";
        public static final String VALUE_1 = "1";
        public static final String IDB64 = "idb64";
        public static final String HCE = "HCE";

        public static final String IP_ADDRESS = "127.0.0.1";

        public static final String OS_NAME = "iOS";
        public static final String IOS_VERSION = "13.3.1";
        public static final String DEVICE = "iPhone 7";
        public static final String BRAND = "Apple";
        public static final String LANGUAGE = "en";
        public static final String PLATFORM = "iOS";
    }

    public static final class AccountType {
        public static final Integer CHECKING_ACCOUNT_GROUP_NUMBER = 1;
        public static final Integer SAVINGS_ACCOUNT_GROUP_NUMBER = 2;
        public static final Integer INVESTMENT_ACCOUNT_GROUP_NUMBER = 5;
    }

    static final class AccountIbanDetails {
        static final int MODE_BENEFICIAIRE_TRUE = 1;
    }

    public static final class TransactionDescriptionFormatting {
        public static final String MERCHANT_NAME = "merchantName";
        public static final String REGEX =
                String.format("^FACTURE CARTE DU [0-9]+ (?<%s>.*) CARTE [0-9X]+$", MERCHANT_NAME);
    }
}
