package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataFetchingPolicy {
    Psd2DataFetchingPolicy psd2DataFetchingPolicy;

    public Psd2DataFetchingPolicy getPsd2DataFetchingPolicy() {
        return psd2DataFetchingPolicy;
    }

    public void setPsd2DataFetchingPolicy(Psd2DataFetchingPolicy psd2DataFetchingPolicy) {
        this.psd2DataFetchingPolicy = psd2DataFetchingPolicy;
    }
}
