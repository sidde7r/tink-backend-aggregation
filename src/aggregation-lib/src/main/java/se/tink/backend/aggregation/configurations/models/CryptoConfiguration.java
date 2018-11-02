package se.tink.backend.aggregation.configurations.models;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "crypto_configurations")
public class CryptoConfiguration {
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

    public String getCryptoId() {
        return cryptoConfigurationId.getClusterId();
    }

    public void setCryptoId(String cryptoId) {
        this.cryptoConfigurationId.setClusterId(cryptoId);
    }

    public String getBase64encodedkey() {
        return base64encodedkey;
    }

    public void setBase64encodedkey(String base64encodedkey) {
        this.base64encodedkey = base64encodedkey;
    }
}
