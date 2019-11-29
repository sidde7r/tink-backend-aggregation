package se.tink.sa.agent.pt.ob.sibs.rest.client;

import static junit.framework.TestCase.assertNotNull;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonSibsRequestRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.framework.tools.JsonUtils;

@Slf4j
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = {"classpath:application.properties", "classpath:secrets.properties"})
@ContextConfiguration(classes = {SibsStandaloneAgent.class})
public class SibsAisRestClientTest extends AbstractRestClientTest {

    @Autowired private SibsAccountInformationClient sibsAccountInformationClient;

    @Test
    public void testAis() {
        String state = UUID.randomUUID().toString().replace("-", "") + UUID_TINK_TAG;
        String bankCode = "BCPPT";
        String consentId = getVerifiedConsentId(state, bankCode);
        assertNotNull(consentId);
        AccountsResponse accountsResponse =
                sibsAccountInformationClient.fetchAccounts(getCommonRequest(consentId, bankCode));
        assertNotNull(accountsResponse);
        log.info("AccountsResponse --> {}", JsonUtils.writeAsJson(accountsResponse));
    }

    private CommonSibsRequestRequest getCommonRequest(String consentId, String bankCode) {
        return CommonSibsRequestRequest.builder().bankCode(bankCode).consentId(consentId).build();
    }
}
