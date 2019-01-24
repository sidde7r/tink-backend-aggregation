package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginWithTokenResponse extends CrossKeyResponse {
    private Date lastLoginDate;
    private String passwordStatus;
    private String deviceToken;

    @JsonIgnore
    public boolean incorrectPassword() {
        return isFailure() && hasAnyErrors(
                CrossKeyConstants.AutoAuthentication.ERR_PASSWORD_NOT_VALID);
    }

    @JsonIgnore
    public boolean passwordExpired() {
        return passwordStatus.equalsIgnoreCase(
                CrossKeyConstants.AutoAuthentication.PASSWORD_STATUS_CHANGE);
    }

    @JsonIgnore
    public boolean isIncorrectDevice() {
        return isFailure() &&
                hasAnyErrors(CrossKeyConstants.AutoAuthentication.ERR_PASSWORD_TOKEN_LOGIN_FAILED
                        , CrossKeyConstants.AutoAuthentication.ERR_PASSWORD_MISSING
                );
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    @JsonFormat(pattern = "yyyyMMdd")
    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getPasswordStatus() {
        return passwordStatus;
    }

    public void setPasswordStatus(String passwordStatus) {
        this.passwordStatus = passwordStatus;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
