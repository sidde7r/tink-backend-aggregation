package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import se.tink.backend.aggregation.nxgen.http.URL;

public class SocieteGeneraleConstants {

    public static class Url {
        private static final String HOST = "https://app.secure.particuliers.societegenerale.mobi";

        public static final URL LOGIN_GRID = new URL(HOST + "/sec/vk/gen_crypto.json");
        public static final URL LOGIN_NUM_PAD = new URL(HOST + "/sec/vk/gen_ui?modeClavier=0&vk_visuel=vk_widescreen");
    }

    public static class QueryParam {
        public static String CRYPTO = "cryptogramme";
    }

    public enum AppAuthenticationValues {
        DEVICE_ID("deviceId", "BEA59C5E-96AD-4F3C-B6EA-E0DD6941EF4F"),
        SERVER_ID("SERVERID", "mobv4_srv22"),
        CONNECTION_TYPE("typeConnexion", "Wifi"),
        OS_VERSION("versionOs", "10.2"),
        DEVICE_NAME("nomTerminal", "iPhone8,1"),
        APPLICATION_NAME("nomApplication", "Société Générale"),
        APP_VERSION("versionApplication", "4.8.1.2"),
        HASHED_SESSION_ID("HASHSESSIONID", "UqF7Cc/zuXYo:000"),
        GDASESSID("GDASESSID", "UIvUawmcC8iRifH1gioqIoUX/+Y="),
        OS("nomOs", "iPhone OS"),
        ;

        private final String key;
        private final String value;

        AppAuthenticationValues(String key, String value) {
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
}
