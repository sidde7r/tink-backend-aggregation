package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils;

import java.util.Base64;

public class HttpAuthUtils {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    public static String createBasicAuth(String username, String password) {
        return String.format(
                "Basic %s",
                encoder.encodeToString(String.format("%s:%s", username, password).getBytes())
        );
    }
}
