package se.tink.backend.sms.gateways.cmtelecom.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Authentication {
    private String productToken;

    public Authentication(String productToken) {
        this.productToken = productToken;
    }
}

