package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.JWT.Claims;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RequiredArgsConstructor
public class RegisterInitJWEManager {

    private final BancoPostaStorage storage;

    public String genActivationJWE() {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(storage.getAppId())
                .setJwtClaimsSet(getActivationClaims())
                .setRSAEnrypter(storage.getPubServerKey())
                .build();
    }

    public String genRegisterJWE(String initCodeChallenge) {
        return new JWE.Builder()
                .setJWEHeader()
                .setJwtClaimsSet(getRegisterClaims(initCodeChallenge))
                .setRSAEnrypter(storage.getPubServerKey())
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
                .setOtpSpecClaims(storage.getOtpSecretKey(), storage.getAppId())
                .setSubject(Claims.ACTIVATION)
                .setData(ImmutableMap.of())
                .build();
    }

    private JWTClaimsSet getOpenIdAzClaims(String username, String password) {
        return new JWEClaims.Builder()
                .setClaim(Claims.APP_ID, storage.getAppId())
                .setClaim(Claims.USERNAME, username)
                .setClaim(Claims.PASSWORD, password)
                .build();
    }

    private JWTClaimsSet getRegisterClaims(String initCodeChallenge) {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setSubject(Claims.REGISTER)
                .setData(getRegisterDataClaims(initCodeChallenge, storage.getKeyPair().getPublic()))
                .build();
    }

    private Map<Object, Object> getRegisterDataClaims(
            String initCodeChallenge, PublicKey pubAppKey) {
        return ImmutableMap.builder()
                .put(
                        Claims.XDEVICE,
                        EncodingUtils.encodeAsBase64String(UUID.randomUUID().toString())
                                + Claims.DEVICE_SPEC_ENCODED)
                .put(Claims.PUB_APP_KEY, EncodingUtils.encodeAsBase64String(pubAppKey.getEncoded()))
                .put(Claims.INIT_CODE_VERIFIER, initCodeChallenge)
                .build();
    }
}
