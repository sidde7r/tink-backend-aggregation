package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.JWT.Claims;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@AllArgsConstructor
public class RegisterInitJWEManager {
    private UserContext userContext;

    private static final String X_DEVICE_VAL =
            ":MDBmYzEzYWRmZjc4NTEyMmI0YWQyODgwOWEzNDIwOTgyMzQxMjQxNDIxMzQ4MDk3ODc4ZTU3N2M5OTFkZThmMA==:IOS:13.3.1:iPhone:13.65.22:true";

    public String genActivationJWE() {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(userContext.getAppId())
                .setJwtClaimsSet(getActivationClaims())
                .setRSAEnrypter(userContext.getPubServerKey())
                .build();
    }

    public String genRegisterJWE(String initCodeChallenge) {
        return new JWE.Builder()
                .setJWEHeader()
                .setJwtClaimsSet(getRegisterClaims(initCodeChallenge))
                .setRSAEnrypter(userContext.getPubServerKey())
                .build();
    }

    public String genAZTokenJWE(String username, String password) {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(JWEAccessTokenConstants.TOKEN_KEY_ID)
                .setJwtClaimsSet(getOpenIdAzClaims(username, password))
                .setRSAEnrypter((RSAPublicKey) JWEAccessTokenConstants.getRSATokenPublicKey())
                .build();
    }

    private JWTClaimsSet getActivationClaims() {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setOtpSpecClaims(userContext.getOtpSecretKey(), userContext.getAppId())
                .setSubject("activation")
                .setData(ImmutableMap.of())
                .build();
    }

    private JWTClaimsSet getOpenIdAzClaims(String username, String password) {
        return new JWEClaims.Builder()
                .setClaim(Claims.APP_ID, userContext.getAppId())
                .setClaim("username", username)
                .setClaim("password", password)
                .build();
    }

    private JWTClaimsSet getRegisterClaims(String initCodeChallenge) {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setSubject("register")
                .setData(
                        getRegisterDataClaims(
                                initCodeChallenge, userContext.getKeyPair().getPublic()))
                .build();
    }

    private Map<Object, Object> getRegisterDataClaims(
            String initCodeChallenge, PublicKey pubAppKey) {
        return ImmutableMap.builder()
                .put(
                        "xdevice",
                        EncodingUtils.encodeAsBase64String(UUID.randomUUID().toString())
                                + X_DEVICE_VAL)
                .put("pubAppKey", EncodingUtils.encodeAsBase64String(pubAppKey.getEncoded()))
                .put("initCodeVerifier", initCodeChallenge)
                .build();
    }
}
