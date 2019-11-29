package se.tink.sa.agent.pt.ob.sibs.rest.client;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import se.tink.sa.agent.pt.ob.sibs.common.service.TppService;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentAccessEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusResponse;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = {"classpath:application.properties", "classpath:secrets.properties"})
@ContextConfiguration(classes = {SibsStandaloneAgent.class})
public class SibsConsentRestClientTest {

    private static final String UUID_TINK_TAG = "feed";

    @Autowired private SibsConsentRestClient sibsConsentRestClient;

    @Autowired private TppService tppService;

    @Test
    public void test() {
        String state = UUID.randomUUID().toString().replace("-", "") + UUID_TINK_TAG;
        String bankCode = "BCPPT";

        ConsentRequest consentRequest = getConsentRequest();
        ConsentResponse consent = sibsConsentRestClient.getConsent(consentRequest, bankCode, state);
        assertNotNull(consent);
        assertNotNull(consent.getLinks());
        assertTrue(StringUtils.isNotBlank(consent.getLinks().getRedirect()));
        tppService.authUser(consent.getLinks().getRedirect(), state);
        ConsentStatusResponse consentStatusResponse =
                sibsConsentRestClient.checkConsentStatus(
                        prepareConsentStatusRequest(consent.getConsentId()), bankCode);
        assertNotNull(consentStatusResponse);
    }

    private ConsentStatusRequest prepareConsentStatusRequest(String consentId) {
        ConsentStatusRequest consentStatusRequest = new ConsentStatusRequest();
        consentStatusRequest.setConsentId(consentId);
        return consentStatusRequest;
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
