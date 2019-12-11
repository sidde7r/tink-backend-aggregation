package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CbiGlobeAuthenticatorTest {
    private CbiGlobeAuthenticator objectToTest;
    private CbiGlobeApiClient apiClient;
    private CbiGlobeConfiguration cbiGlobeConfiguration;
    private PersistentStorage persistentStorage;

    @Before
    public void init() {
        apiClient = Mockito.mock(CbiGlobeApiClient.class);
        persistentStorage = Mockito.mock(PersistentStorage.class);
        cbiGlobeConfiguration = Mockito.mock(CbiGlobeConfiguration.class);
        objectToTest =
                new CbiGlobeAuthenticator(apiClient, persistentStorage, cbiGlobeConfiguration);
    }

    @Test
    public void consentValidPeriodShouldBe89Days() {
        ConsentRequest consentRequest = objectToTest.createConsentRequestAccount();
        LocalDate localDate = LocalDate.parse(consentRequest.getValidUntil());
        Assert.assertEquals(localDate, LocalDate.now().plusDays(89));
    }
}
