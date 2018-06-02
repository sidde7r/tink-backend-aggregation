package se.tink.backend.rpc;

import java.util.Date;
import java.util.Set;
import se.tink.libraries.auth.AuthenticationMethod;

public class UserProfileResponse {
    private String username;
    private String nationalId;
    private Date created;
    private Set<AuthenticationMethod> authorizedLoginMethods;
    private Set<AuthenticationMethod> availableLoginMethods;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Set<AuthenticationMethod> getAuthorizedLoginMethods() {
        return authorizedLoginMethods;
    }

    public void setAuthorizedLoginMethods(Set<AuthenticationMethod> authorizedLoginMethods) {
        this.authorizedLoginMethods = authorizedLoginMethods;
    }

    public Set<AuthenticationMethod> getAvailableLoginMethods() {
        return availableLoginMethods;
    }

    public void setAvailableLoginMethods(Set<AuthenticationMethod> availableLoginMethods) {
        this.availableLoginMethods = availableLoginMethods;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
