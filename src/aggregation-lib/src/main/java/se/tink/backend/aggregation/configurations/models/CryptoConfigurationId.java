package se.tink.backend.aggregation.configurations.models;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class CryptoConfigurationId implements Serializable {
    @NotNull
    @Column(name = "cryptoId")
    private String cryptoId;
    @NotNull
    @Column(name = "keyid")
    private int keyId;

    public String getClusterId() {
        return cryptoId;
    }

    public void setClusterId(String clusterId) {
        this.cryptoId = clusterId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public CryptoConfigurationId() {
    }

    public CryptoConfigurationId(String clusterId, int keyId) {
        this.cryptoId = clusterId;
        this.keyId = keyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CryptoConfigurationId cryptoId = (CryptoConfigurationId) o;
        return keyId == cryptoId.keyId &&
                Objects.equals(cryptoId, cryptoId.cryptoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cryptoId, keyId);
    }

    public boolean isValid() {
        if (Objects.isNull(cryptoId)) {
            return false;
        }

        if (keyId == 0) {
            return false;
        }

        return true;
    }
}
