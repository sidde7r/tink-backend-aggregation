package se.tink.backend.core;

import java.util.Objects;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "cluster_crypto_configurations")
public class ClusterCryptoConfiguration {
    private static final Base64 BASE64 = new Base64();

    @EmbeddedId
    private CryptoId cryptoId;
    @NotNull
    @Type(type = "text")
    private String base64EncodedKey;

    public CryptoId getCryptoId() {
        return cryptoId;
    }

    public void setCryptoId(CryptoId cryptoId) {
        this.cryptoId = cryptoId;
    }

    public String getBase64EncodedKey() {
        return base64EncodedKey;
    }

    public byte[] getDecodedKey() {
        return BASE64.decode(base64EncodedKey);
    }

    public void setBase64EncodedKey(String base64EncodedKey) {
        this.base64EncodedKey = base64EncodedKey;
    }

    public boolean isValid() {
        if (Objects.isNull(cryptoId)) {
            return false;
        }

        if (!cryptoId.isValid()) {
            return false;
        }

        if (Objects.isNull(base64EncodedKey)) {
            return false;
        }

        return true;
    }
}
