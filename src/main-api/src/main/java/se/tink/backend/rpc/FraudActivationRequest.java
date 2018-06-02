package se.tink.backend.rpc;

public class FraudActivationRequest {

    private String personIdentityNumber;
    private boolean activate;
    
    public boolean isActivate() {
        return activate;
    }
    public void setActivate(boolean activate) {
        this.activate = activate;
    }
    public String getPersonIdentityNumber() {
        return personIdentityNumber;
    }
    public void setPersonIdentityNumber(String personIdentityNumber) {
        this.personIdentityNumber = personIdentityNumber;
    }
}
