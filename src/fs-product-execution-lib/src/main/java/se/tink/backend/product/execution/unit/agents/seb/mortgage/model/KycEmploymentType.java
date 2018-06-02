package se.tink.backend.product.execution.unit.agents.seb.mortgage.model;

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
