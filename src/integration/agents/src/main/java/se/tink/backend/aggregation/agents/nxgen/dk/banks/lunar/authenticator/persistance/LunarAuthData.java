package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonObject
public class LunarAuthData {
    private String userId;
    private String nemIdPassword;
    private String lunarPassword;
    private String deviceId;
    private String accessToken;

    public boolean hasCredentials() {
        return ObjectUtils.allNotNull(
                getUserId(), getLunarPassword(), getDeviceId(), getAccessToken());
    }
}
