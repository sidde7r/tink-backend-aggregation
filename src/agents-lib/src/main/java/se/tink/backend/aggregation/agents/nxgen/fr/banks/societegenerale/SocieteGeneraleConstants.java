package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import se.tink.backend.aggregation.nxgen.http.URL;

public class SocieteGeneraleConstants {

    public static class QueryParam {
        public static final String CRYPTOGRAMME = "cryptogramme";
        public static final String NIV_AUTHENT = "niv_authent";
        public static final String MODE_CLAVIER = "modeClavier";
        public static final String VK_VISUEL = "vk_visuel";
    }

    public static class Url {

        private static final String HOST = "https://app.secure.particuliers.societegenerale.mobi";

        public static final URL SEC_VK_GEN_CRYPTO = new URL(HOST + "/sec/vk/gen_crypto.json");
        public static final URL SEC_VK_GEN_UI = new URL(HOST + "/sec/vk/gen_ui");
        public static final URL SEC_VK_AUTHENT = new URL(HOST + "/sec/vk/authent.json");
        public static final URL GET_AUTH_INFO = new URL(HOST + "/getauthinfo.json");
    }

    public class StorageKey {
        public static final String DEVICE_ID = "device_id";
        public static final String TOKEN = "token";
    }

}
