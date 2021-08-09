package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AccessType {
    ALL_ACCOUNTS("allAccounts"),
    ALL_ACCOUNTS_WITH_OWNER_NAME("allAccountsWithOwnerName");

    private final String code;

    @JsonValue
    public String getCode() {
        return code;
    }
}
