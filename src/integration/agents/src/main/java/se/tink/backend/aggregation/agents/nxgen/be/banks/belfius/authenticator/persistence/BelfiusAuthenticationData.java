package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils.formatPanNumber;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
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

    public String getPanNumber() {
        if (null == panNumber) {
            return panNumber;
        }
        return formatPanNumber(panNumber);
    }

    public void clearData() {
        deviceToken = null;
        panNumber = null;
        password = null;
    }
}
