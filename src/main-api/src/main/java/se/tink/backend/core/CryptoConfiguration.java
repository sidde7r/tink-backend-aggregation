package se.tink.backend.core;

import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "crypto_configuration")
public class CryptoConfiguration {
    /*
    @Id
    private int keyId;
    @Type(type = "text")
    private String cryptoId;
   */
    @EmbeddedId
    private CryptoConfigurationId cryptoConfigurationId;

    @Type(type = "text")
    private String base64encodedkey;

    public CryptoConfiguration() {
        // Ok.
    }

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
