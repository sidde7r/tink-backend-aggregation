package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.MobileChallengeRequestedToken;

public class BancoBpiUserStateTest {

    private BancoBpiAuthContext objectUnderTest;

    private static final String PIN = "1234";
    private static final String DEVICE_UUID = "1234567890";
    private static final String CSRF_TOKEN = "token";

    @Before
    public void init() {
        objectUnderTest = new BancoBpiAuthContext();
    }

    @Test
    public void clearAuthDataTest() {
        // given
        initObjectUnderTestWithRegistrationData();
        // when
        objectUnderTest.clearAuthData();
        // then
        Assert.assertNull(objectUnderTest.getAccessPin());
        Assert.assertNull(objectUnderTest.getDeviceUUID());
        Assert.assertNull(objectUnderTest.getMobileChallengeRequestedToken());
        Assert.assertNull(objectUnderTest.getSessionCSRFToken());
        Assert.assertFalse(objectUnderTest.isDeviceActivationFinished());
    }

    @Test
    public void finishDeviceActivationTest() {
        // given
        initObjectUnderTestWithRegistrationData();
        // when
        objectUnderTest.finishDeviceActivation();
        // then
        Assert.assertEquals(PIN, objectUnderTest.getAccessPin());
        Assert.assertEquals(DEVICE_UUID, objectUnderTest.getDeviceUUID());
        Assert.assertEquals(CSRF_TOKEN, objectUnderTest.getSessionCSRFToken());
        Assert.assertTrue(objectUnderTest.isDeviceActivationFinished());
        Assert.assertNull(objectUnderTest.getMobileChallengeRequestedToken());
    }

    private void initObjectUnderTestWithRegistrationData() {
        objectUnderTest.setAccessPin("1234");
        objectUnderTest.setMobileChallengeRequestedToken(new MobileChallengeRequestedToken());
        objectUnderTest.setDeviceUUID("1234567890");
        objectUnderTest.setSessionCSRFToken("token");
    }
}
