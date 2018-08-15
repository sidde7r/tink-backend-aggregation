package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import se.tink.backend.aggregation.nxgen.http.URL;

public class BnpParibasConstants {

    public static final class Urls {
        public static final String HOST = "https://m-service.bnpparibas.net/";
        public static final URL NUMPAD = new URL(HOST + "ident-mobile-wspl/rpc/grille.do");
        public static final URL LOGIN = new URL(HOST + "SEEA-pa01/devServer/seeaserver");
        public static final URL KEEP_ALIVE = new URL(HOST + "authentforte-wspl/rpc/recupererStatut");
        public static final URL USER_OVERVIEW = new URL(HOST + "serviceinfosclient-wspl/rpc/InfosClient");
        public static final URL ACCOUNT_DETAILS = new URL(HOST + "SmcDiversPresentationWeb/EntryPointSmcDiversServlet");
        public static final URL TRANSACTIONAL_ACCOUNT_TRANSACTIONS = new URL(HOST + "rop-wspl/rest/releveOp");
    }

    public static final class Storage {
        public static final String IDFA_UUID = "idfa";
        public static final String IDFV_UUID = "idfv";
        public static final String LOGIN_ID = "loginId";
        public static final String IBAN_KEY = "ibanKey";
    }

    public static final class Auth {
        // This has to be the user agent for transaction fetching to work
        public static final String USER_AGENT = "MesComptes/127 CFNetwork/889.9 Darwin/17.2.0";
        public static final int NUMPAD_SIZE = 10;
        public static final String INDEX_0 = "0";
        public static final String INDEX_1 = "1";

        public static final String GRID_TYPE = "typeGrille";
        public static final String GRID_TYPE_V4iOS = "mesComptesV4iOS_MOB";
        public static final String AUTH = "AUTH";
    }

    public static final class AuthFormValues {
        public static final String DIST_ID = "BNPNetParticulier";
        public static final String MEAN_ID = "MOBILEWEBSEAL";
        public static final String ID_GRILLE = "idGrille";
        public static final String POS_SELECT = "posSelect";
        public static final String VALUE_1 = "1";
        public static final String IDB64 = "idb64";
        public static final String HCE = "HCE";

        public static final String USER_AGENT = "MesComptes/127 CFNetwork/889.9 Darwin/17.2.0";
        public static final String IP_ADDRESS = "127.0.0.1";

        public static final String OS_NAME = "iOS";
        public static final String IOS_VERSION = "11.1.1";
        public static final String DEVICE = "iPhone 7";
        public static final String BRAND = "Apple";
        public static final String LANGUAGE = "en";

        public static final String APP_VERSION = "4.6.2";
        public static final String BUILD_NUMBER = "127";
        public static final String PLATFORM = "iOS";
    }

    public static final class QueryParams {
        public static final String ACCOUNT_NUMBER = "numCompte";
        public static final String MODE_APPEL = "modeAppel";
        public static final String MODE_APPEL_0 = "0";
        public static final String SERVICE = "service";
        public static final String SERVICE_RIB = "detailRIB";
    }

    public static final class AccountType {
        public static final String CHECKING = "Compte de chèques";
    }

    public static final class TransactionalAccountTransactions {
        public static final String TRIAV = "1";
        public static final String PAST_OR_PENDING = "2";
    }

    public static final class TransactionDescriptionFormatting {
        public static final String MERCHANT_NAME = "merchantName";
        public static final String REGEX = String.format(
                "^FACTURE CARTE DU [0-9]+ (?<%s>.*) CARTE [0-9X]+$", MERCHANT_NAME);
    }

    public static final class Tags {
        public static final String UNKNOWN_ACCOUNT_TYPE = "fr_bnpparibas_unknown_account_type";
    }
}
