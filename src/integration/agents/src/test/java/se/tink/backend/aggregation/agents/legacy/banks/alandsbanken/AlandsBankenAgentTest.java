package se.tink.backend.aggregation.agents.banks.alandsbanken;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.se.alandsbanken.AlandsBankenAgent;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;

public class AlandsBankenAgentTest extends AbstractAgentTest<AlandsBankenAgent> {

    // Sensitive payload keys
    private static final String DEVICE_ID = "MFazmX+zZzbrz1xMA9JvVTBMD7fM/AQXNQiBRXR3pS0=";
    private static final String DEVICE_TOKEN =
            "VhuhaazMd5o5ZQi9xGUtogiS2sfT+6B2l6Qr4QleW4CBt+9GhfdHqnI30rkcuvfbDI3eIGNEZ1N83ueI+nGSUg==";
    private static final String PAYLOAD =
            "{\"deviceToken\":\"" + DEVICE_TOKEN + "\",\"deviceId\":\"" + DEVICE_ID + "\"}";
    private static final String USERNAME = "ASK_DANIEL";
    private static final String PASSWORD = "ASK_DANIEL";

    public AlandsBankenAgentTest() {
        super(AlandsBankenAgent.class);
    }

    @Test
    @Ignore("Broken test")
    public void testUser1() throws Exception {
        testAgent(USERNAME, PASSWORD);
    }

    @Test
    @Ignore("Broken test")
    public void testExistingUser() throws Exception {
        testAgentWithSensitivePayload(USERNAME, PASSWORD, PAYLOAD);
    }
}
