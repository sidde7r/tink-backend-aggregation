package se.tink.backend.core;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class CryptoId implements Serializable {
    @NotNull
    @Column(name = "clusterid")
    private String clusterId;
    @NotNull
    @Column(name = "keyid")
    private int keyId;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public CryptoId() {
    }

    public CryptoId(String clusterId, int keyId) {
        this.clusterId = clusterId;
        this.keyId = keyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CryptoId cryptoId = (CryptoId) o;
        return keyId == cryptoId.keyId &&
                Objects.equals(clusterId, cryptoId.clusterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterId, keyId);
    }

    public boolean isValid() {
        if (Objects.isNull(clusterId)) {
            return false;
        }

        if (keyId == 0) {
            return false;
        }

        return true;
    }
}
