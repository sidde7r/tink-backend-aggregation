package se.tink.sa.agent.pt.ob.sibs.rest.client;

import static org.junit.Assert.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = {"classpath:application.properties", "classpath:secrets.properties"})
@ContextConfiguration(classes = {SibsStandaloneAgent.class})
public class SibsConsentRestClientTest extends AbstractRestClientTest {

    @Test
    public void testConsent() {
        String state = generateNewTestState();
        String bankCode = "BCPPT";
        String consentId = getVerifiedConsentId(state, bankCode);
        assertNotNull(consentId);
    }
}
