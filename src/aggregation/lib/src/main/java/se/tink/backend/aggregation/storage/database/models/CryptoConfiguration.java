package se.tink.backend.aggregation.storage.database.models;

import java.util.Base64;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "crypto_configurations")
public class CryptoConfiguration {
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    @EmbeddedId
    private CryptoConfigurationId cryptoConfigurationId;
    @Type(type = "text")
    private String base64encodedkey;
    
    public int getKeyId() {
        return cryptoConfigurationId.getKeyId();
    }

    public void setKeyId(int keyId) {
        this.cryptoConfigurationId.setKeyId(keyId);
    }

    public CryptoConfigurationId getCryptoConfigurationId() {
        return cryptoConfigurationId;
    }

    public void setCryptoConfigurationId(
            CryptoConfigurationId cryptoConfigurationId) {
        this.cryptoConfigurationId = cryptoConfigurationId;
    }

    public String getBase64encodedkey() {
        return base64encodedkey;
    }

    public void setBase64encodedkey(String base64encodedkey) {
        this.base64encodedkey = base64encodedkey;
    }

    public byte[] getDecodedKey() {
        return BASE64_DECODER.decode(base64encodedkey);
    }

}
