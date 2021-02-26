package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonObject
public class LunarAuthData {
    private String userId;
    private String nemIdPassword;
    private String deviceId;
    private String accessToken;
    private AccountsResponse accountsResponse;

    public boolean hasCredentials() {
        return ObjectUtils.allNotNull(getUserId(), getDeviceId(), getAccessToken());
    }
}
