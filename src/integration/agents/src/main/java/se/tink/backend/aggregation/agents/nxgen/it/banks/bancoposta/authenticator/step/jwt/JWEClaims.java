package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.JWT.Claims;
import se.tink.backend.aggregation.agents.utils.crypto.HOTP;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class JWEClaims {

    public static class Builder {

        private static final int OTP_SPECS_CLAIM_DIGIT_CODE = 8;
        private static final int DEFAULT_TRUNCATION_OFFSET_HOTP = 20;

        private JWTClaimsSet.Builder jwtClaimSetBuilder = new JWTClaimsSet.Builder();

        public Builder setDefaultValues() {
            this.jwtClaimSetBuilder
                    .issueTime(new Date())
                    .expirationTime(new Date())
                    .issuer(Claims.APP_BPOL)
                    .jwtID(UUID.randomUUID().toString())
                    .notBeforeTime(new Date());
            return this;
        }

        public Builder setOtpSpecClaims(byte[] otpSecretKey, String appId) {
            this.jwtClaimSetBuilder
                    .claim(Claims.OTP_SPECS, getOtpSpecClaims(otpSecretKey))
                    .claim(Claims.KID_SHA_256, getKidSHA(appId));
            return this;
        }

        public Builder setSubject(String subject) {
            this.jwtClaimSetBuilder.subject(subject);
            return this;
        }

        public Builder setData(Object data) {
            this.jwtClaimSetBuilder.claim(Claims.DATA, data);
            return this;
        }

        public Builder setClaim(String claim, String value) {
            this.jwtClaimSetBuilder.claim(claim, value);
            return this;
        }

        public JWTClaimsSet build() {
            return jwtClaimSetBuilder.build();
        }

        private static String getKidSHA(String appId) {
            return EncodingUtils.encodeAsBase64String(Hash.sha256(appId));
        }

        public static Map<String, String> getOtpSpecClaims(byte[] otpSecretKey) {
            long movingFactor = new SecureRandom().nextInt();
            String hotp =
                    HOTP.generateOTP(
                            otpSecretKey,
                            movingFactor,
                            OTP_SPECS_CLAIM_DIGIT_CODE,
                            DEFAULT_TRUNCATION_OFFSET_HOTP);

            return ImmutableMap.<String, String>builder()
                    .put(Claims.TYPE, Claims.HMAC_SHA_1)
                    .put(Claims.MOVING_FACTOR, String.valueOf(movingFactor))
                    .put(Claims.OTP, hotp)
                    .build();
        }
    }
}
