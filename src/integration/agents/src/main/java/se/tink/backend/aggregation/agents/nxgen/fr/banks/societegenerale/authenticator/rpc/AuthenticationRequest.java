package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AuthenticationRequest extends AbstractForm {

    public static AuthenticationRequest create(
            String user_id, String cryptocvcs, String codsec, String deviceId, String token) {
        AuthenticationRequest retVal = new AuthenticationRequest();
        retVal.put(
                SocieteGeneraleConstants.FormParam.BIO_TOKEN,
                SocieteGeneraleConstants.Default.EMPTY);
        retVal.put(SocieteGeneraleConstants.FormParam.CIBLE, SocieteGeneraleConstants.Default._300);
        retVal.put(SocieteGeneraleConstants.FormParam.CODSEC, codsec);
        retVal.put(SocieteGeneraleConstants.FormParam.CRYPTOCVCS, cryptocvcs);
        retVal.put(SocieteGeneraleConstants.FormParam.DPE, deviceId);
        retVal.put(SocieteGeneraleConstants.FormParam.JETON, token);
        retVal.put(SocieteGeneraleConstants.FormParam.USER_ID, user_id);
        retVal.put(SocieteGeneraleConstants.FormParam.VK_OP, SocieteGeneraleConstants.Default.AUTH);
        return retVal;
    }
}
