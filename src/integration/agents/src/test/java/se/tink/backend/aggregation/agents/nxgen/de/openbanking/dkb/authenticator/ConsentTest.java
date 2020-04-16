package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ConsentTest {

    @Test
    public void isValidShouldReturnTrueForValidConsentStatus() {
        // given
        Consent tested = new Consent();
        tested.setConsentStatus("valid");

        // when
        boolean result = tested.isValid();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isValidShouldReturnTrueForOtherConsentStatus() {
        // given
        Consent tested = new Consent();
        tested.setConsentStatus("received");

        // when
        boolean result = tested.isValid();

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void isNotAuthorizedShouldReturnTrueForReceivedConsentStatus() {
        // given
        Consent tested = new Consent();
        tested.setConsentStatus("received");

        // when
        boolean result = tested.isNotAuthorized();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isNotAuthorizedShouldReturnTrueForOtherConsentStatus() {
        // given
        Consent tested = new Consent();
        tested.setConsentStatus("valid");

        // when
        boolean result = tested.isNotAuthorized();

        // then
        assertThat(result).isFalse();
    }
}
