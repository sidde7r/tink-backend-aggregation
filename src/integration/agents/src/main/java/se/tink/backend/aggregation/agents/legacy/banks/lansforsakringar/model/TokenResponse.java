package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponse {
    protected int number;
    protected String numberPair;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getNumberPair() {
        return numberPair;
    }

    public void setNumberPair(String numberPair) {
        this.numberPair = numberPair;
    }
}
