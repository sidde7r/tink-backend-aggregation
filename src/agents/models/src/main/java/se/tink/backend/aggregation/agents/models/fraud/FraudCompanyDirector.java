package se.tink.backend.aggregation.agents.models.fraud;

public class FraudCompanyDirector {

    private String name;
    private String role;
    
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object obj) {
        FraudCompanyDirector director = (FraudCompanyDirector) obj;
        return (director.getName().equals(this.getName()) && director.getRole().equals(this.getRole()));
    }
}
