package se.tink.backend.aggregation.workers.encryption;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AesEncryptedData {
    private byte[] iv;
    private byte[] data;

    public byte[] getIv() {
        return iv;
    }

    public AesEncryptedData setIv(byte[] iv) {
        this.iv = iv;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public AesEncryptedData setData(byte[] data) {
        this.data = data;
        return this;
    }
}
