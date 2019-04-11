package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class LoginRequest extends AbstractForm {

    private LoginRequest(String username, String bpiMetaData, String xorPin) {
        this.put(LclConstants.Authentication.CODE_ID_XOR, xorPin);
        this.put(LclConstants.Authentication.LCL_BPI_METADATA, bpiMetaData);
        this.put(
                LclConstants.AuthenticationValuePairs.DEVICE.getKey(),
                LclConstants.AuthenticationValuePairs.DEVICE.getValue());
        this.put(LclConstants.Authentication.IDENTIFIANT, username);
        this.put(
                LclConstants.AuthenticationValuePairs.IDENTIFIANT_ROUTING.getKey(),
                LclConstants.AuthenticationValuePairs.IDENTIFIANT_ROUTING.getValue());
        this.put(
                LclConstants.AuthenticationValuePairs.MOBILE.getKey(),
                LclConstants.AuthenticationValuePairs.MOBILE.getValue());
    }

    public static LoginRequest create(String username, String bpiMetaData, String xorPin) {
        return new LoginRequest(username, bpiMetaData, xorPin);
    }
}
