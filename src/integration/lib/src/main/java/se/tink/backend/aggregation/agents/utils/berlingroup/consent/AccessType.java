package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@EqualsAndHashCode
@JsonObject
public class AccessType {
    public static final AccessType ALL_ACCOUNTS = new AccessType("allAccounts");
    public static final AccessType ALL_ACCOUNTS_WITH_OWNER_NAME =
            new AccessType("allAccountsWithOwnerName");

    private final String code;

    @JsonValue
    public String getCode() {
        return code;
    }
}
