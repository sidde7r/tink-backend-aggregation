package se.tink.backend.aggregation.workers.encryption;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncryptedCredentialsV1 extends EncryptedCredentials {
    @JsonIgnore
    public static final int VERSION = 1;

    private AesEncryptedData fields;
    private AesEncryptedData payload;

    public EncryptedCredentialsV1() {
        setVersion(VERSION);
    }

    public AesEncryptedData getFields() {
        return fields;
    }

    public EncryptedCredentialsV1 setFields(AesEncryptedData fields) {
        this.fields = fields;
        return this;
    }

    public AesEncryptedData getPayload() {
        return payload;
    }

    public EncryptedCredentialsV1 setPayload(AesEncryptedData payload) {
        this.payload = payload;
        return this;
    }

    @JsonIgnore
    public byte[] getVersionAsAAD() {
        String s = Integer.toString(getVersion());
        return s.getBytes();
    }

    @JsonIgnore
    public byte[] getTimestampAsAAD() {
        Date timestamp = getTimestamp();
        String s = Long.toString(timestamp.getTime());
        return s.getBytes();
    }

    @JsonIgnore
    public byte[] getKeyIdAsAAD() {
        String s = Integer.toString(getKeyId());
        return s.getBytes();
    }
}
