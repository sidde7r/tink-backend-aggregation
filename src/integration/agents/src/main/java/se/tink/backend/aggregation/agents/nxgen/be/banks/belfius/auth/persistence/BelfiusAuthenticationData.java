package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonObject
public class BelfiusAuthenticationData {

    private String password;
    private String panNumber;
    private String deviceToken;

    public boolean hasCredentials() {
        return ObjectUtils.allNotNull(getPanNumber(), getPassword(), getDeviceToken());
    }

    public BelfiusAuthenticationData password(String password) {
        this.password = password;
        return this;
    }

    public BelfiusAuthenticationData panNumber(String panNumber) {
        this.panNumber = panNumber;
        return this;
    }

    public BelfiusAuthenticationData deviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }
}
