package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;

public class CbiConsentCreationStepTest {

    private static final String TEST_CONSENT_ID = "1234509876";

    @Test
    public void shouldCallApiClientAndSaveConsentIdAfterSuccesfulResponse() {
        // given
        CbiGlobeAuthApiClient mockAuthApiClient = mock(CbiGlobeAuthApiClient.class);
        CbiStorage mockStorage = mock(CbiStorage.class);

        CbiConsentCreationStep consentCreationStep =
                new CbiConsentCreationStep(
                        mockAuthApiClient, new ConstantLocalDateTimeSource(), mockStorage);

        when(mockAuthApiClient.createConsent(
                        TestDataReader.readFromFile(
                                TestDataReader.CREATE_CONSENT_REQ, ConsentRequest.class)))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CREATE_CONSENT_RESP, CbiConsentResponse.class));

        // when
        consentCreationStep.createConsentAndSaveId();

        // then
        verify(mockStorage).saveConsentId(TEST_CONSENT_ID);
    }
}
