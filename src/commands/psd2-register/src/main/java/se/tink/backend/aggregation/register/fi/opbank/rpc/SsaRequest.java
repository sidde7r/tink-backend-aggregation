package se.tink.backend.aggregation.register.fi.opbank.rpc;

import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants.Option;
import se.tink.backend.aggregation.register.fi.opbank.utils.PSD2Utils;

public class SsaRequest {

    public static JSONObject create(final String ssa) {
        final int iat = (int) Math.floor(System.currentTimeMillis() / 1000);
        final int exp = (int) (System.currentTimeMillis() / 1000) + 6 * 60 * 60;
        final JSONObject ssaRequest = new JSONObject();

        ssaRequest.put("iat", PSD2Utils.generateCurrentTime());
        ssaRequest.put("exp", PSD2Utils.generateCurrentTime() + 6 * 60 * 60);
        ssaRequest.put("aud", Option.AUD);
        ssaRequest.put("jti", PSD2Utils.generateRandomUUID());
        ssaRequest.put("redirect_uris", Option.SOFTWARE_REDIRECT_URIS);
        ssaRequest.put("grant_types", Option.GRANT_TYPES);
        ssaRequest.put("software_statement", ssa);

        return ssaRequest;
    }
}
