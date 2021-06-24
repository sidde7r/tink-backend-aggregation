package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.LogTags.LUNAR_TAG;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonObject
@Slf4j
public class LunarAuthData {
    private String userId;
    private String nemIdPassword;
    private String deviceId;
    private String accessToken;
    private String lunarUserId;
    private AccountsResponse accountsResponse;

    public boolean hasCredentials() {
        boolean userIdNotNull = userId != null;
        boolean deviceIdNotNull = deviceId != null;
        boolean accessTokenNotNull = accessToken != null;
        log.info(
                "{} UserId is not null: {}, DeviceId is not null: {}, AccessToken is not null: {}",
                LUNAR_TAG,
                userIdNotNull,
                deviceIdNotNull,
                accessTokenNotNull);

        return userIdNotNull && deviceIdNotNull && accessTokenNotNull;
    }
}
