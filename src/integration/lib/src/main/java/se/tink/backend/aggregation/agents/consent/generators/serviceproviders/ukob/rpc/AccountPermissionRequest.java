package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.entities.AccountPermissionDataEntity;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.ukob.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AccountPermissionRequest {
    @JsonProperty("Data")
    private final AccountPermissionDataEntity data;

    @JsonProperty("Risk")
    private final RiskEntity risk;

    public static AccountPermissionRequest of(Set<String> permissions) {
        return new AccountPermissionRequest(
                AccountPermissionDataEntity.of(permissions), new RiskEntity());
    }

    public static AccountPermissionRequest of(Set<String> permissions, String expirationDateTime) {
        return new AccountPermissionRequest(
                AccountPermissionDataEntity.of(permissions, expirationDateTime), new RiskEntity());
    }
}
