package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.CbiConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;

@RunWith(JUnitParamsRunner.class)
public class CbiGlobeAutoAuthenticatorTest {

    private static final String TEST_CONSENT_ID = "1234509876";

    private CbiGlobeAuthApiClient mockAuthApiClient;
    private CbiStorage mockStorage;

    private CbiGlobeAutoAuthenticator cbiGlobeAutoAuthenticator;

    @Before
    public void setup() {
        mockAuthApiClient = mock(CbiGlobeAuthApiClient.class);
        mockStorage = mock(CbiStorage.class);

        cbiGlobeAutoAuthenticator = new CbiGlobeAutoAuthenticator(mockAuthApiClient, mockStorage);
    }

    @Test
    public void shouldThrowConsentInvalidWhenNoConsentInStorage() {
        // given

        // when
        Throwable throwable = catchThrowable(() -> cbiGlobeAutoAuthenticator.autoAuthenticate());

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .extracting("error")
                .isEqualTo(SessionError.CONSENT_INVALID);
    }

    @Test
    @Parameters(method = "possibleConsentStatusesAndExceptions")
    public void shouldThrowExpectedExceptionBasedOnConsentStatus(
            String consentStatusReturnedByBank, AgentError expectedError) {
        // given
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID))
                .thenReturn(
                        new CbiConsentStatusResponse(
                                new CbiConsentStatus(consentStatusReturnedByBank)));

        // when
        Throwable throwable = catchThrowable(() -> cbiGlobeAutoAuthenticator.autoAuthenticate());

        // then
        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .extracting("error")
                .isEqualTo(expectedError);
    }

    private Object[] possibleConsentStatusesAndExceptions() {
        return new Object[] {
            new Object[] {"expired", SessionError.CONSENT_EXPIRED},
            new Object[] {"pendingExpired", SessionError.CONSENT_EXPIRED},
            new Object[] {"received", SessionError.CONSENT_INVALID},
            new Object[] {"rejected", SessionError.CONSENT_INVALID},
            new Object[] {"terminatedByTpp", SessionError.CONSENT_INVALID},
            new Object[] {"partiallyAuthorised", SessionError.CONSENT_INVALID},
            new Object[] {"revokedByPsu", SessionError.CONSENT_REVOKED_BY_USER},
            new Object[] {"replaced", SessionError.CONSENT_REVOKED},
            new Object[] {"invalidated", SessionError.CONSENT_REVOKED},
        };
    }

    @Test
    public void shouldFinishWithoutExceptionWhenStoredConsentStillValid() {
        // given
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockAuthApiClient.fetchConsentStatus(TEST_CONSENT_ID))
                .thenReturn(new CbiConsentStatusResponse(new CbiConsentStatus("valid")));

        // when & then
        assertThatCode(() -> cbiGlobeAutoAuthenticator.autoAuthenticate())
                .doesNotThrowAnyException();
    }
}
