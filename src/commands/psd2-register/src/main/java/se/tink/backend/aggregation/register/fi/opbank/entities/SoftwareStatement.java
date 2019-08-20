package se.tink.backend.aggregation.register.fi.opbank.entities;

import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants.Option;
import se.tink.backend.aggregation.register.fi.opbank.utils.PSD2Utils;

public class SoftwareStatement {

    public static JSONObject create() {
        final JSONObject ssaJson = new JSONObject();

        ssaJson.put("iss", Option.TPP_ID);
        ssaJson.put("iat", PSD2Utils.generateCurrentTime());
        ssaJson.put("exp", PSD2Utils.generateCurrentTime() + 6 * 60 * 60);
        ssaJson.put("jti", PSD2Utils.generateRandomUUID());
        ssaJson.put("software_client_id", "Tink-" + PSD2Utils.generateRandomUUID());
        ssaJson.put("software_roles", Option.SOFTWARE_ROLES);
        ssaJson.put("software_jwks_endpoint", Option.SOFTWARE_JWKS_ENDPOINT);
        ssaJson.put("software_jwks_revoked_endpoint", Option.SOFTWARE_JWKS_REVOKED_ENDPOINT);
        ssaJson.put("software_client_name", Option.SOFTWARE_CLIENT_NAME);
        ssaJson.put("software_redirect_uris", Option.SOFTWARE_REDIRECT_URIS);
        ssaJson.put("software_client_uri", Option.CLIENT_URI);
        ssaJson.put("org_name", Option.ORGANIZATION_NAME);
        ssaJson.put("org_id", Option.TPP_ID);

        return ssaJson;
    }
}
