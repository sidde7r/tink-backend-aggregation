package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AllPsd2 {
    ALL_ACCOUNTS("allAccounts"),
    ALL_ACCOUNTS_WITH_OWNER_NAME("allAccountsWithOwnerName");

    private final String psdCode;

    @JsonValue
    public String getPsdCode() {
        return psdCode;
    }
}
