package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

public class SecurityEntity {
    private String security;
    private String typeOfSecurity;

    public SecurityEntity() {

    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getTypeOfSecurity() {
        return typeOfSecurity;
    }

    public void setTypeOfSecurity(String typeOfSecurity) {
        this.typeOfSecurity = typeOfSecurity;
    }
}
