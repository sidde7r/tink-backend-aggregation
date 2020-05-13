package se.tink.sa.agent.pt.ob.sibs.rest.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.SibsUtils;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentAccessEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusResponse;

public class AbstractRestClientTest {

    protected static final String UUID_TINK_TAG = "feed";

    @Autowired private SibsConsentRestClient sibsConsentRestClient;

    @Autowired private se.tink.sa.agent.pt.ob.sibs.common.service.TppService tppService;

    protected String getVerifiedConsentId(String state, String bankCode) {
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
        return consent.getConsentId();
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

    protected String generateNewTestState() {
        return UUID.randomUUID().toString().replace("-", "") + UUID_TINK_TAG;
    }
}
