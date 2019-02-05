package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator;

import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapConfiguration;

public class AktiaEncapConfiguration implements EncapConfiguration {
    @Override
    public String getApplicationVersion() {
        return "115";
    }

    @Override
    public String getEncapApiVersion() {
        return "3.3.6";
    }

    @Override
    public String getCredentialsAppNameForEdb() {
        return "AKTIA_MOBILE_BANK";
    }

    @Override
    public String getCredentialsBankCodeForEdb() {
        return "(null)";
    }

    @Override
    public String getSaIdentifier() {
        return "samobile_aktia_ios_v1";
    }

    @Override
    public String getAppId() {
        return "com.aktia.mobilebank";
    }

    @Override
    public String getRsaPubKeyString() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3maOiHUOhZR75rlXiyic" +
                "csi5mp5OEdkamnC1oRO1o71eP2u7v3i3sEIHQ9jHaIw6kHCrHqFCPvgjvbzcM8vC" +
                "uHZF3xafYCxShUH6Kb5AU7of6L7dTqXJDwyK6EJ1sGX1qIrlqVdYzDtfEES7NZb4" +
                "nJOpcFzeG9Nt9N7slm4Xq7KFYHFSkVXOWF2Se9f/raoaYVkFCNK8XClw1wPRnkc0" +
                "587xE1qwUa661m/pmCkm6M0FO7wfdS9zOQuq9Ual1x2sD7q+H2UhKtmY9zb31paM" +
                "ZDa6Tr3/eHopfisV/g1LxeVx/99tVf7b3vdAbBlcBep6YaawnhWM27NGEZ/jldzK" +
                "YQIDAQAB";
    }

    @Override
    public String getClientPrivateKeyString() {
        return "MIICXAIBAAKBgQCa2xyT5mGEPAA2V5aAPFI+8VhClxM5e8588QEjBdNqRZ6uSKfg" +
                "2CImo6ONTIM/6RadqtSIun0uMtLcI23GmAmGKn7NoPOKztLcDFXJSnTiBwC3yXhw" +
                "liTFNd+Y8UjKZz4kggD9nW3ytkeqfVLRztpEP5N27cRXRGflip/jGoSHUwIDAQAB" +
                "AoGAWuXE55fyo9Eoer19DTbbPDEkkqnlUfe3ZCV/elRgHeBR3ZGuYU/c8/tX9If1" +
                "/tzONqxg5wU4l5ajqS5usITsCVhDdAquqwO6qanmewN75xFfeUoazMwlYJ7YiUsJ" +
                "rwObIGJsy+A3LZjb5WQqBRJ1xGATPcC52vjxy5USqgcgWrkCQQDJY5n2znN/Rgor" +
                "R3q58HMyJQt28yN8vWBemOoWEXPedGl94PYY3VQHWQ/Xd6EdfN81WsAygnJNfXxp" +
                "Yw/SlYHFAkEAxNkvYMq9+axXMGz2y7mNxid8Xw691JK0TaYmi0lfN3/zKIFVH/3w" +
                "IUvtlcn8eFrhW4ttIyPX/u3yjOpyLUduNwJAPY50QTxZkU2XTiNLIAqfK3SnTHSF" +
                "JFu+WSvkYVp0UErE8/UPRApi5NwUO4gVdy30DBrxJH868PSqUow1CekpAQJBAJFS" +
                "PPXZ9tHhdySa7L+NpqqI7/pFKcNK4q0IYiAl9JNGdD6M7EkH8UTDhwwz55z6irBI" +
                "iDOO/KCJDa0WT2A6AZUCQHjJSaVi9BrhQ/vG6PiowgZ7wIrEtMCzo18pNeKCvn2F" +
                "BTBgya7Ulkp+5zNWvJDKZ/TA2VWkdE+cg/ZyhazgLRk=";
    }
}
