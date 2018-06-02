package se.tink.backend.system.document.core;

import java.util.Optional;

public class Residence {

    private final String mortgageProvider;
    private final Optional<String> housingCooperative;
    private final ResidenceType type;

    public Residence(String mortgageProvider, Optional<String> housingCooperative, ResidenceType type) {
        this.mortgageProvider = mortgageProvider;
        this.housingCooperative = housingCooperative;
        this.type = type;
    }

    public String getMortgageProvider() {
        return mortgageProvider;
    }

    public Optional<String> getHousingCooperative() {
        return housingCooperative;
    }

    public ResidenceType getType() {
        return type;
    }
}
