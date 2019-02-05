package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

public class ConfirmTanCodeRequest {

    private String tan;

    public String getTan() {
        return tan;
    }

    public ConfirmTanCodeRequest setTan(String tan) {
        this.tan = tan;
        return this;
    }
}
