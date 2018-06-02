package se.tink.backend.core;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import io.protostuff.Tag;

public class FraudDetailsAnswer {

    @Tag(1)
    private String text;
    @Tag(2)
    @Enumerated(EnumType.STRING)
    private FraudStatus status;
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public FraudStatus getStatus() {
        return status;
    }
    public void setStatus(FraudStatus status) {
        this.status = status;
    }
}
