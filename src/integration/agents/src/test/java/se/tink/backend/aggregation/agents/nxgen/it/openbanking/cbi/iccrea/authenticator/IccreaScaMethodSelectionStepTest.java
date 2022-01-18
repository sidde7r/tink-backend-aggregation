package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaScaMethodSelectionStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.SelectAuthorizationMethodRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IccreaScaMethodSelectionStepTest {

    private static final URL TEST_URL_FOR_OPERATION = new URL("https://www.example.com");

    private CbiGlobeAuthApiClient mockAuthApiClient;

    private IccreaScaMethodSelectionStep scaMethodSelectionStep;

    @Before
    public void setup() {

        mockAuthApiClient = mock(CbiGlobeAuthApiClient.class);

        scaMethodSelectionStep =
                new IccreaScaMethodSelectionStep(mockAuthApiClient, TEST_URL_FOR_OPERATION);
    }

    @Test
    public void shouldExecuteCallWithExpectedUrlAndMethodWhenMethodsAvailable() {
        // given
        CbiConsentResponse cbiConsentResponse =
                TestDataReader.readFromFile(
                        TestDataReader.METHOD_SELECTION, CbiConsentResponse.class);
        // when
        scaMethodSelectionStep.pickScaMethod(cbiConsentResponse);

        // then
        verify(mockAuthApiClient)
                .selectScaMethod(
                        TEST_URL_FOR_OPERATION.concat("/consents/1234509876"),
                        new SelectAuthorizationMethodRequest("2"));
    }

    @Test
    public void shouldThrowNoAvailableMethodsWhenNoMethods() {
        // given
        CbiConsentResponse cbiConsentResponse =
                TestDataReader.readFromFile(
                        TestDataReader.METHOD_SELECTION_NO_METHODS, CbiConsentResponse.class);

        // when
        Throwable throwable =
                catchThrowable(() -> scaMethodSelectionStep.pickScaMethod(cbiConsentResponse));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .extracting("error")
                .isEqualTo(LoginError.NO_AVAILABLE_SCA_METHODS);
    }
}
