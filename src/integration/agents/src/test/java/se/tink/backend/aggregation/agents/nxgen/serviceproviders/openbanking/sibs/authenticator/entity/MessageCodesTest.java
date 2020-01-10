package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity;

import org.junit.Assert;
import org.junit.Test;

public class MessageCodesTest {

    @Test
    public void isConsentProblemShouldReturnTrueWhenConsentIsInvalid() {
        // given
        final String message = "CONSENT_INVALID";
        // when
        boolean result = MessageCodes.isConsentProblem(message);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void isConsentProblemShouldReturnTrueWhenConsentIsExpired() {
        // given
        final String message = "CONSENT_EXPIRED";
        // when
        boolean result = MessageCodes.isConsentProblem(message);
        // then
        Assert.assertTrue(result);
    }

    @Test
    public void isConsentProblemShouldReturnTrueWhenConsentIsUnknown() {
        // given
        final String message = "CONSENT_UNKNOWN";
        // when
        boolean result = MessageCodes.isConsentProblem(message);
        // then
        Assert.assertTrue(result);
    }
}
