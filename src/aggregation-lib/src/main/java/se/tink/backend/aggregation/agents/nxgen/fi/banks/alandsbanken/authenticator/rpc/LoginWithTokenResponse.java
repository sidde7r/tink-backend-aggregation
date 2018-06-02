package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;

public class LoginWithTokenResponse extends AlandsBankenResponse {
    //TODO: Are all these fields needed?
    private Date lastLoginDate;
    private String passwordStatus;
    private String deviceToken;

    public boolean incorrectPassword() {
        return isFailure() && hasAnyErrors(
                AlandsBankenConstants.AutoAuthentication.ERR_PASSWORD_NOT_VALID);
    }

    public boolean isIncorrectDevice() {
        return isFailure() &&
                hasAnyErrors(AlandsBankenConstants.AutoAuthentication.ERR_PASSWORD_TOKEN_LOGIN_FAILED
                        , AlandsBankenConstants.AutoAuthentication.ERR_PASSWORD_MISSING
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
