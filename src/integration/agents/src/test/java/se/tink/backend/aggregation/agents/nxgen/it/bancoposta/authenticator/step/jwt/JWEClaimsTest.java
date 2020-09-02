package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step.AuthenticationTestData;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.JWEClaims;

public class JWEClaimsTest {
    private static final String CLAIM_KEY = "claimKey";
    private static final String CLAIM_VAL = "claimVal";
    private static final String DATA_CLAIM_VAL = "dataClaimVal";
    private static final String SUBJECT = "subject";

    @Test
    public void jWEClaimsBuilderShouldBuildJWEClaimsSetInBuilder() {
        // given
        // when
        JWTClaimsSet claims =
                new JWEClaims.Builder()
                        .setOtpSpecClaims(
                                BaseEncoding.base32().decode(AuthenticationTestData.OTP_SECRET_KEY),
                                AuthenticationTestData.APP_ID)
                        .setClaim(CLAIM_KEY, CLAIM_VAL)
                        .setData(DATA_CLAIM_VAL)
                        .setDefaultValues()
                        .setSubject(SUBJECT)
                        .build();
        // then
        assertThat(claims.getClaims())
                .containsKeys("otp-specs", "kid-sha256", "iat", "exp", "iss", "jti", "nbf")
                .containsEntry("sub", SUBJECT)
                .containsEntry(CLAIM_KEY, CLAIM_VAL)
                .containsEntry("data", DATA_CLAIM_VAL);
    }
}
