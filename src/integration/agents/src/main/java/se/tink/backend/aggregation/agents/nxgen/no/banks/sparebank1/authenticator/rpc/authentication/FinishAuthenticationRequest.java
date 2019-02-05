package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinishAuthenticationRequest {
    private String m1;
    private String publicA;

    public String getM1() {
        return m1;
    }

    public void setM1(String m1) {
        this.m1 = m1;
    }

    public String getPublicA() {
        return publicA;
    }

    public void setPublicA(String publicA) {
        this.publicA = publicA;
    }
}
