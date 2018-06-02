package se.tink.backend.product.execution.unit.agents.seb.mortgage.model;

public enum EmploymentType {
    PERMANENT_EMPLOYMENT("TILLSVIDARE"),
    TEMPORARY_EMPLOYMENT("VISSTID"),
    SELF_EMPLOYED("EGENFÖRETAGARE"),
    UNEMPLOYED("ARBETSLÖS"),
    STUDENT("STUDENT"),
    PENSIONER("PENSIONÄR"),
    OTHER("ÖVRIGT");

    private final String key;

    EmploymentType(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
