package se.tink.sa.agent.pt.ob.sibs.rest.client;

import static junit.framework.TestCase.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent;
import se.tink.sa.agent.pt.ob.sibs.SibsUtils;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentAccessEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = {"classpath:application.properties", "classpath:secrets.properties"})
@ContextConfiguration(classes = {SibsStandaloneAgent.class})
public class SibsConsentRestClientTest {

    @Autowired private SibsConsentRestClient sibsConsentRestClient;

    @Test
    public void test() {
        ConsentRequest consentRequest = getConsentRequest();
        ConsentResponse consent = sibsConsentRestClient.getConsent(consentRequest);
        assertNotNull(consent);
    }

    private ConsentRequest getConsentRequest() {
        String valid90Days = SibsUtils.get90DaysValidConsentStringDate();
        return new ConsentRequest(
                new ConsentAccessEntity(SibsConstants.FormValues.ALL_ACCOUNTS),
                true,
                valid90Days,
                SibsConstants.FormValues.FREQUENCY_PER_DAY,
                false);
    }
}
