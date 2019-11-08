package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

public interface JwtClaims {
    String ALGORITHM = "alg";
    String CONTENT_TYPE = "cty";
    String CRIT = "crit";
    String TYPE = "typ";
    String KEY_ID = "kid";
    String ISSUER = "iss";
    String SUBJECT = "sub";
    String EXPIRES_AT = "exp";
    String NOT_BEFORE = "nbf";
    String ISSUED_AT = "iat";
    String JWT_ID = "jti";
    String AUDIENCE = "aud";
}
