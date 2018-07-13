package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    @JsonProperty("UserInstallationId")
    private String userInstallationId;

    @JsonProperty("IsAbove16")
    private boolean above16;

    @JsonProperty("IsAbove18")
    private boolean above18;

    @JsonProperty("IsStudent")
    private boolean student;

    @JsonProperty("Kdk")
    private KnowYourCustomer knowYourCustomer;

    public String getUserInstallationId() {
        return userInstallationId;
    }

    public void setUserInstallationId(String userInstallationId) {
        this.userInstallationId = userInstallationId;
    }

    public boolean isAbove16() {
        return above16;
    }

    public void setAbove16(boolean above16) {
        this.above16 = above16;
    }

    public boolean isAbove18() {
        return above18;
    }

    public void setAbove18(boolean above18) {
        this.above18 = above18;
    }

    public boolean isStudent() {
        return student;
    }

    public void setStudent(boolean student) {
        this.student = student;
    }

    public KnowYourCustomer getKnowYourCustomer() {
        return knowYourCustomer;
    }

    public void setKnowYourCustomer(KnowYourCustomer knowYourCustomer) {
        this.knowYourCustomer = knowYourCustomer;
    }
}
