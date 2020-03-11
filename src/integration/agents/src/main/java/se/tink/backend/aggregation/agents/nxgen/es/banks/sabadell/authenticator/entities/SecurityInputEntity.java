package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.InitiateSessionRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityInputEntity {
    private String authenticationType = "";
    private String clearText = "";
    private String deviceId = "";
    private FloatingKeyboardEntity floatingKeyboard;
    private String iberKey1 = "";
    private String iberKey2 = "";
    private String mobileClearText = "";
    private String mobileCompany = "";
    private String mobilePhone = "";
    private String password = "";
    private String random = "";
    private String riskCode = "";
    private String secondPassword = "";
    private String signText = "";
    private String timeStamp = "";

    public static SecurityInputEntity of(String keyboardKey, String scaPassword) {
        final SecurityInputEntity entity = new SecurityInputEntity();
        entity.authenticationType = Authentication.TYPE_SCA;
        entity.password = scaPassword;
        entity.floatingKeyboard =
                FloatingKeyboardEntity.of(
                        InitiateSessionRequest.FLOATING_KEYBOARD_ENABLED,
                        InitiateSessionRequest.FLOATING_KEYBOARD_KEY_PREFIX + keyboardKey);
        return entity;
    }
}
