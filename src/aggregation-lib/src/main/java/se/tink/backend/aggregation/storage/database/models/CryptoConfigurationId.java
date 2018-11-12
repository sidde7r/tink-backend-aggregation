package se.tink.backend.aggregation.storage.database.models;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class CryptoConfigurationId implements Serializable {
    @NotNull
    @Column(name = "clientname")
    private String clientName;
    @NotNull
    @Column(name = "keyid")
    private int keyId;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public static CryptoConfigurationId of(int keyId, String clientName) {
        CryptoConfigurationId cryptoConfigurationId = new CryptoConfigurationId();
        cryptoConfigurationId.setClientName(clientName);
        cryptoConfigurationId.setKeyId(keyId);
        return cryptoConfigurationId;
    }

    public boolean isValid() {
        if (Objects.isNull(clientName)) {
            return false;
        }

        if (keyId == 0) {
            return false;
        }

        return true;
    }
}
