package se.tink.backend.aggregation.workers.encryption;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncryptedCredentials {
    private int version;
    private Date timestamp;
    private int keyId;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    @JsonIgnore
    public String asSerialized() {
        return SerializationUtils.serializeToString(this);
    }
}
