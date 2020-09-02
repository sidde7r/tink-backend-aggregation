package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.JWT.Claims;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity.SignedChallenge;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.utils.crypto.HOTP;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class FinalizeAuthJWEManager {

    private UserContext userContext;

    public String genCheckRegisterJWE() {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(userContext.getAppId())
                .setJwtClaimsSet(buildCheckRegisterClaims())
                .setRSAEnrypter(userContext.getPubServerKey())
                .build();
    }

    public String genChallengeJWE() {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(userContext.getAppId())
                .setJwtClaimsSet(buildChallengeClaims())
                .setRSAEnrypter(userContext.getPubServerKey())
                .build();
    }

    public String genAuthorizeTransactionJWE(ChallengeResponse challengeResponse) {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(userContext.getAppId())
                .setJwtClaimsSet(
                        buildAuthorizeTransactionsClaims(
                                userContext.getUserPin(), challengeResponse))
                .setRSAEnrypter(userContext.getPubServerKey())
                .build();
    }

    public String genAccessTokenJWE(String signature, String transactionId) {
        return new JWE.Builder()
                .setJWEHeaderWithKeyId(JWEAccessTokenConstants.TOKEN_KEY_ID)
                .setJwtClaimsSet(buildAccessTokenClaims(transactionId, signature))
                .setRSAEnrypter((RSAPublicKey) JWEAccessTokenConstants.getRSATokenPublicKey())
                .build();
    }

    private Map<String, String> genCheckRegisterDataClaims() {
        return ImmutableMap.of(Claims.APP_REGISTER_ID, userContext.getAppRegisterId());
    }

    private JWTClaimsSet buildAuthorizeTransactionsClaims(
            String userPin, ChallengeResponse challengeResponse) {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setOtpSpecClaims(userContext.getOtpSecretKey(), userContext.getAppId())
                .setData(getAuthorizeTransactionClaims(userPin, challengeResponse))
                .build();
    }

    private Map<String, String> getAuthorizeTransactionClaims(
            String userPin, ChallengeResponse challengeResponse) {
        byte[] key =
                (challengeResponse.getTransactionId() + ":" + userContext.getSecretApp())
                        .getBytes();
        long movingFactor = Long.parseLong(challengeResponse.getRandK());
        String otp = HOTP.generateOTP(key, movingFactor, 6, 20);

        return ImmutableMap.<String, String>builder()
                .put("transaction-challenge", challengeResponse.getTransactionChallenge())
                .put(Claims.APP_REGISTER_ID, userContext.getAppRegisterId())
                .put("authzTool", "posteid")
                .put("sigtype", "JWS")
                .put(Claims.USER_PIN, userPin)
                .put("transactionID", challengeResponse.getTransactionId())
                .put(Claims.OTP, otp)
                .build();
    }

    private JWTClaimsSet buildChallengeClaims() {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setOtpSpecClaims(userContext.getOtpSecretKey(), userContext.getAppId())
                .setData(getChallengeDataClaims(userContext.getAppRegisterId()))
                .build();
    }

    private Map<String, String> getChallengeDataClaims(String appRegisterId) {
        return ImmutableMap.of(Claims.APP_REGISTER_ID, appRegisterId);
    }

    private JWTClaimsSet buildCheckRegisterClaims() {
        return new JWEClaims.Builder()
                .setDefaultValues()
                .setOtpSpecClaims(userContext.getOtpSecretKey(), userContext.getAppId())
                .setData(genCheckRegisterDataClaims())
                .build();
    }

    private JWTClaimsSet buildAccessTokenClaims(String transactionId, String signature) {
        return new JWEClaims.Builder()
                .setClaim(Claims.APP_ID, userContext.getAppId())
                .setClaim("signed_challenge", getSignedChallengeString(transactionId, signature))
                .build();
    }

    private String getSignedChallengeString(String transactionId, String signature) {
        return SerializationUtils.serializeToString(
                new SignedChallenge(transactionId, "JWS", signature));
    }
}
