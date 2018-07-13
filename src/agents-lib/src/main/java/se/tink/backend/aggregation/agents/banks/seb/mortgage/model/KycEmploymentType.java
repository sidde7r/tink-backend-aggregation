package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

public enum KycEmploymentType {
    EMPLOYED("ANSTÄLLD"),
    STUDENT("STUDENT"),
    SELF_EMPLOYED("EGENFÖRETAGARE"),
    PENSIONER("PENSIONÄR"),
    UNEMPLOYED("ARBETSLÖS"),
    OTHER("ANNAT");

    private final String key;

    KycEmploymentType(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
