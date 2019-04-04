package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet;

public abstract class NordnetConstants {

    public static class Keys {
        public static final String PUBLIC_KEY =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5td/64fAicX2r8sN6RP"
                        + "3mfHf2bcwvTzmHrLcjJbU85gLROL+IXclrjWsluqyt5xtc/TCwMTfC/NcRVIAvfZdt+OPdDoO0rJYIY3hOGB"
                        + "wLQJeLRfruM8dhVD+/Kpu8yKzKOcRdne2hBb/mpkVtIl5avJPFZ6AQbICpOC8kEfI1DHrfgT18fBswt85deI"
                        + "LBTxVUIXsXdG1ljFAQ/lJd/62J74vayQJq6l2DT663QB8nLEILUKEt/hQAJGU3VT4APSfT+5bkClfRb9+kNT"
                        + "7RXT/pNCctbBTKujr3tmkrdUZiQiJZdl/O7LhI99nCe6uyJ+la9jNPOuK5z6v72cXenmKZwIDAQAB";
    }

    public static class Urls {
        public static final String BASE_PATH = "https://api.test.nordnet.se";
        public static final String LOGIN_PATH = BASE_PATH + "/next/2/login";
        public static final String GET_ACCOUNTS_PATH = BASE_PATH + "/next/2/accounts";
        public static final String GET_ACCOUNT_DETAILS_PATH =
                GET_ACCOUNTS_PATH + "/{accountNumber}";
    }

    public static class IdTags {
        public static final String ACCOUNT_NUMBER = "accountNumber";
    }

    public static class StorageKeys {
        public static final String SESSION_KEY = "session-key";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class HeaderValues {
        public static final String AUTHORIZATION_PREFIX = "Basic ";
    }

    public static class FormKeys {
        public static final String AUTH = "auth";
        public static final String SERVICE = "service";
    }

    public static class FormValues {
        public static final String SERVICE = "NEXTAPI";
    }
}
