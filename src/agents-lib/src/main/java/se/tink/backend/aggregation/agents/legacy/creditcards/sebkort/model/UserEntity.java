package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {
    protected String firstName;
    protected String surName;
    protected String phone;
    protected String email;
    protected boolean otpAuthentication;
    protected boolean passwordAuthentication;
    protected int autoenticationTypeId;
    protected boolean coBrowse;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isOtpAuthentication() {
        return otpAuthentication;
    }

    public void setOtpAuthentication(boolean otpAuthentication) {
        this.otpAuthentication = otpAuthentication;
    }

    public boolean isPasswordAuthentication() {
        return passwordAuthentication;
    }

    public void setPasswordAuthentication(boolean passwordAuthentication) {
        this.passwordAuthentication = passwordAuthentication;
    }

    public int getAutoenticationTypeId() {
        return autoenticationTypeId;
    }

    public void setAutoenticationTypeId(int autoenticationTypeId) {
        this.autoenticationTypeId = autoenticationTypeId;
    }

    public boolean isCoBrowse() {
        return coBrowse;
    }

    public void setCoBrowse(boolean coBrowse) {
        this.coBrowse = coBrowse;
    }
}
