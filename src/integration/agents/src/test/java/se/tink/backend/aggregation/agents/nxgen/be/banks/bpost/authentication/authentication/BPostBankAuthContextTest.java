package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication;

import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BPostBankAuthContextTest {

    private BPostBankAuthContext objectUnderTest;
    private final String subscriptionNo = "0197523322";
    private final String pin = "111111";

    @Before
    public void init() {
        objectUnderTest = new BPostBankAuthContext();
        objectUnderTest.setPin(pin);
        objectUnderTest.setSubscriptionNo(subscriptionNo);
    }

    @Test
    public void generateDeviceRootedHashShouldGenerateRandom64LengthCharSequence() {
        // given
        Pattern pattern = Pattern.compile("^[a-z\\d]{64}$");
        // when
        String result = objectUnderTest.generateRandomDeviceRootedHash();
        // then
        Assert.assertTrue(pattern.matcher(result).matches());
    }

    @Test
    public void getDeviceCredentialShouldReturnProperValue() {
        // given
        final String expectedDeviceCredential =
                "eea700b93d1e52364ab4c467d68a11b68be89f0a12c03e3d073def7becd3150c";
        // when
        String result = objectUnderTest.getDeviceCredential();
        // then
        Assert.assertEquals(expectedDeviceCredential, result);
    }

    @Test
    public void getDataMapCodeShouldReturnProperValue() {
        // given
        final String expectedValue =
                "d30bada5eed6ff45916e04419fb67bde60957dfb2c7a2bcb5afb46026a725914";
        final String sessionToken = "HYFVFKI5W1ORU4GP7XL4RWF196TGVRYM";
        objectUnderTest.setSessionToken(sessionToken);
        // when
        String result = objectUnderTest.getDataMapCode();
        // then
        Assert.assertEquals(expectedValue, result);
    }
}
