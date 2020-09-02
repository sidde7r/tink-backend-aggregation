package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt;

import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.JWT.Claims;
import se.tink.backend.aggregation.agents.utils.crypto.HOTP;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class JWEClaims {

    public static class Builder {

        private JWTClaimsSet.Builder jwtClaimSetBuilder = new JWTClaimsSet.Builder();

        public Builder setDefaultValues() {
            this.jwtClaimSetBuilder
                    .issueTime(new Date())
                    .expirationTime(new Date())
                    .issuer("app-bpol")
                    .jwtID(UUID.randomUUID().toString())
                    .notBeforeTime(new Date());
            return this;
        }

        public Builder setOtpSpecClaims(byte[] otpSecretKey, String appId) {
            this.jwtClaimSetBuilder
                    .claim("otp-specs", getOtpSpecClaims(otpSecretKey))
                    .claim("kid-sha256", getKidSHA(appId));
            return this;
        }

        public Builder setSubject(String subject) {
            this.jwtClaimSetBuilder.subject(subject);
            return this;
        }

        public Builder setData(Object data) {
            this.jwtClaimSetBuilder.claim("data", data);
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
            long movingFactor = new Random().nextInt();
            String hotp = HOTP.generateOTP(otpSecretKey, movingFactor, 8, 20);

            return ImmutableMap.<String, String>builder()
                    .put("type", "HMAC-SHA1")
                    .put("movingFactor", String.valueOf(movingFactor))
                    .put(Claims.OTP, hotp)
                    .build();
        }
    }
}
