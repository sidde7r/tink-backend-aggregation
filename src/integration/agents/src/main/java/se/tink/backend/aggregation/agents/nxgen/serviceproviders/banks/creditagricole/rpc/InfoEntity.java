package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InfoEntity {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
