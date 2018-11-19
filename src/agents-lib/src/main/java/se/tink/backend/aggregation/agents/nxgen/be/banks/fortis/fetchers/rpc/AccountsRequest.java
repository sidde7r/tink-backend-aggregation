package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsRequest {
    private String viewId = "";
    private boolean flagAllAccounts = false;
}
