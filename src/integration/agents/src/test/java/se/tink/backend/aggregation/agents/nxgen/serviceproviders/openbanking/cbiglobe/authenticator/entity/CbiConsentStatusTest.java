package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class CbiConsentStatusTest {

    @Test
    @Parameters({
        "replaced",
        "invalidated",
        "pendingExpired",
        "rejected",
        "valid",
        "revokedByPsu",
        "expired",
        "terminatedByTpp"
    })
    public void shouldBeFinal(String consentStatus) {
        // given
        CbiConsentStatus cbiConsentStatus = new CbiConsentStatus(consentStatus);

        // when
        boolean isFinal = cbiConsentStatus.isFinal();

        // then
        assertThat(isFinal).isTrue();
    }

    @Test
    @Parameters({"received", "partiallyAuthorised", "somethingElse"})
    public void shouldNotBeFinal(String consentStatus) {
        // given
        CbiConsentStatus cbiConsentStatus = new CbiConsentStatus(consentStatus);

        // when
        boolean isFinal = cbiConsentStatus.isFinal();

        // then
        assertThat(isFinal).isFalse();
    }
}
