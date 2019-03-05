package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BackendUserResponseEntity {
    private String clientId;
    private String accountingTerminal;
    private String clientStatus;

    public String getClientId() {
        return clientId;
    }

    public String getAccountingTerminal() {
        return accountingTerminal;
    }

    public String getClientStatus() {
        return clientStatus;
    }
}
