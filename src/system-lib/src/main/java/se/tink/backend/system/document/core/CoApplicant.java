package se.tink.backend.system.document.core;

public class CoApplicant {

    private final String name;
    private final String nationalId;

    public CoApplicant(String name, String nationalId) {
        this.name = name;
        this.nationalId = nationalId;
    }

    public String getName() {
        return name;
    }

    public String getNationalId() {
        return nationalId;
    }
}
