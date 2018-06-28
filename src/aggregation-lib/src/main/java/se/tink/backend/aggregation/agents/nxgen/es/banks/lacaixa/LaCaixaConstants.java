package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import se.tink.backend.aggregation.nxgen.http.URL;

public class LaCaixaConstants {

    public static class ApiService {
        public static final String LOGIN_INIT_PATH = "login/loginInicio";
        public static final String LOGIN_SUBMIT_PATH = "login/loginResultado";
        public static final String MAIN_ACCOUNT_PATH = "dashboardApp/cuentaPrincipal?";
        public static final String CHECK_FOTO = "smartContent/consultaFoto"; // Used for keep alive. TODO: Evaluate
        public static final String USER_DATA = "login/loginDatosUsuario";
    }

    public static class Urls{
        private static final String BASE = "https://loapp.caixabank.es/xmlapps/rest/";

        public static final URL INIT_LOGIN = new URL(BASE + ApiService.LOGIN_INIT_PATH); // Gets session id. Needed before login.
        public static final URL SUBMIT_LOGIN = new URL(BASE + ApiService.LOGIN_SUBMIT_PATH);
        public static final URL FETCH_MAIN_ACCOUNT = new URL(BASE + ApiService.MAIN_ACCOUNT_PATH);
        public static final URL KEEP_ALIVE = new URL(BASE + ApiService.CHECK_FOTO);
        public static final URL FETCH_USER_DATA = new URL(BASE + ApiService.USER_DATA);
    }

    public static class DefaultRequestParams{
        public static final String IDIOMA = "en"; // English TODO: Language constants already exists somewhere?
        public static final String ORIGEN = "4024"; // Can seemingly be anything as long as it exists, purpose unknown.
        public static final String CANAL = "O"; // Only some valid values (1, 2, O, ...), purpose unknown.
        public static final String ID_INSTALACION = "CIAPPLPh7,2CakrHGsSyjX1nakKcEk6dOc3gHc="; // Suspected to be app install ID

    }

    public static class Fetcher{

        public static final String MAX_TRANSACTION_HISTORY = "30";
    }

    public static class StatusCodes{

        public static final int INCORRECT_USERNAME_PASSWORD = 409; // Conflict
    }

    public static class UserData{
        public static final String FULL_HOLDER_NAME = "linkNombreEmp";
    }
}
