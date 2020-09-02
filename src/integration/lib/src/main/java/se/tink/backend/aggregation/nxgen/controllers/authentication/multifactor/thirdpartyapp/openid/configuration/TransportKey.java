package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransportKey {

    private String keyId;
    /*.p12 keystore used for mTLS, b64 encoded. It should contain obwac certficate and private key.
    To generate it use this command:
    `$ openssl pkcs12 -export -inkey obwac.key -in obwac.crt | base64`
    */
    private String key;
    // password to the p12 keystore
    private String password;

    public TransportKey() {}

    public TransportKey(String keyId, String key, String password) {
        this.keyId = keyId;
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
