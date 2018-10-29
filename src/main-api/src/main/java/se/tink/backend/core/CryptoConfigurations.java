package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "crypto_configurations")
public class CryptoConfigurations {
    @Id
    private int keyId;
    @Type(type = "text")
    private String cryptoId;
    @Type(type = "text")
    private String base64encodedkey;

    public CryptoConfigurations() {
        // Ok.
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getCryptoId() {
        return cryptoId;
    }

    public void setCryptoId(String cryptoId) {
        this.cryptoId = cryptoId;
    }

    public String getBase64encodedkey() {
        return base64encodedkey;
    }

    public void setBase64encodedkey(String base64encodedkey) {
        this.base64encodedkey = base64encodedkey;
    }
}
