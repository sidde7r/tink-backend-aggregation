package se.tink.sa.agent.pt.ob.sibs.rest.client;

import static junit.framework.TestCase.assertNotNull;

import java.util.UUID;
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
    public void testeConsent() {
        String state = UUID.randomUUID().toString().replace("-", "") + UUID_TINK_TAG;
        String bankCode = "BCPPT";
        String consentId = getVerifiedConsentId(state, bankCode);
        assertNotNull(consentId);
    }
}
