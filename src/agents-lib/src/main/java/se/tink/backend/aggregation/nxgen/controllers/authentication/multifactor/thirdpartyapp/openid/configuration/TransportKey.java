package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransportKey {

    private String keyId;
    private String key;
    private String password;

    public byte[] getP12Key() {
        return EncodingUtils.decodeBase64String(key);
    }

    public String getPassword() {
        return password;
    }
}
