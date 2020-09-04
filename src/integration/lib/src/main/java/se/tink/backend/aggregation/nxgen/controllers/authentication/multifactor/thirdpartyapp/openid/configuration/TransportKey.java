package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransportKey {

    private String key;
    private String password;

    public TransportKey(String key, String password) {
        this.key = key;
        this.password = password;
    }

    public byte[] getP12Key() {
        return EncodingUtils.decodeBase64String(key);
    }

    public String getPassword() {
        return password;
    }
}
