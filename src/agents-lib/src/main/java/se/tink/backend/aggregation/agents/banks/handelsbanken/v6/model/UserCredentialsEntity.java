package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class UserCredentialsEntity {
    private String code;
    private String personalId;

    public UserCredentialsEntity(String personalid, String code) {
        this.code = code;
        this.personalId = personalid;
    }

    public String getPersonalId() {
        return personalId;
    }

    public void setPersonalid(String personalId) {
        this.personalId = personalId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
