package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginWithInstallIdResponse {

    private String sessionToken;
    //    Other possibly interesting fields:
    //    private String cprNo;
    //    private String personalId;
    //    private String advisor;
    //    private String customerName;
    //    private boolean campaignAvailable;

    public String encrypt(Token token) {
        final byte[] bytes = sessionToken.getBytes(JyskeConstants.CHARSET);
        final byte[] array2 = new byte[16 + bytes.length];
        System.arraycopy(bytes, 0, array2, 16, bytes.length);
        return new String(JyskeSecurityHelper.encryptWithAESAndBase64Encode(array2, token));
    }
}
