package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtHeader {

    private String alg;
    private String typ;

    public JwtHeader(String alg, String typ) {
        this.alg = alg;
        this.typ = typ;
    }
}
