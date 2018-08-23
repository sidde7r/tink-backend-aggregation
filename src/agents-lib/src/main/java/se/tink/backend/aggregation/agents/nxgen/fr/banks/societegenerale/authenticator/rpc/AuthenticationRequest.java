package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AuthenticationRequest extends AbstractForm {

    public static AuthenticationRequest create(String user_id, String cryptocvcs, String codsec,
            String deviceId, String token) {
        AuthenticationRequest retVal = new AuthenticationRequest();
        retVal.put("bio_token", "");
        retVal.put("cible", "300");
        retVal.put("codsec", codsec);
        retVal.put("cryptocvcs", cryptocvcs);
        retVal.put("dpe", deviceId);
        retVal.put("jeton", token);
        retVal.put("user_id", user_id);
        retVal.put("vk_op", "auth");
        return retVal;
    }

}
