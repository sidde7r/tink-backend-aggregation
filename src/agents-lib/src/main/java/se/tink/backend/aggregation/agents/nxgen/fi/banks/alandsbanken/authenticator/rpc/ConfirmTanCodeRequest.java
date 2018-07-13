package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc;

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
