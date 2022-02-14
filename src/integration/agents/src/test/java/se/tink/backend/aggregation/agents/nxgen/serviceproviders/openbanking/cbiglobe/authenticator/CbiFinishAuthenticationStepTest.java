package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;

public class CbiFinishAuthenticationStepTest {

    private static final String TEST_CONSENT_ID = "1234509876";

    private CbiGlobeAuthApiClient mockAuthApiClient;
    private Credentials mockCredentials;
    private CbiStorage mockStorage;

    private CbiFinishAuthenticationStep finishAuthenticationStep;

    @Before
    public void setup() {
        mockAuthApiClient = mock(CbiGlobeAuthApiClient.class);
        mockCredentials = mock(Credentials.class);
        mockStorage = mock(CbiStorage.class);
        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);

        finishAuthenticationStep =
                new CbiFinishAuthenticationStep(mockAuthApiClient, mockCredentials, mockStorage);
    }

    @Test
    public void shouldParseValidUntilWithJustDateAndUpdateCredentials() {
        // given
        when(mockAuthApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_DATE, ConsentDetailsResponse.class));

        // when
        finishAuthenticationStep.storeConsentValidUntilDateInCredentials();

        // then
        verify(mockCredentials).setSessionExpiryDate(LocalDate.of(2022, 4, 22));
    }

    @Test
    public void shouldParseValidUntilWithDatetimeAndUpdateCredentials() {
        // given
        when(mockAuthApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_DATETIME,
                                ConsentDetailsResponse.class));

        // when
        finishAuthenticationStep.storeConsentValidUntilDateInCredentials();

        // then
        verify(mockCredentials).setSessionExpiryDate(LocalDate.of(2022, 4, 25));
    }

    @Test
    public void shouldThrowIllegalStateWhenUnexpectedFormatEncountered() {
        // given
        when(mockAuthApiClient.fetchConsentDetails(TEST_CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_UNEXPECTED,
                                ConsentDetailsResponse.class));

        // when
        Throwable throwable =
                catchThrowable(
                        () -> finishAuthenticationStep.storeConsentValidUntilDateInCredentials());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "[CBI] Could not parse the consent validUntil field using any of the expected formats!");
    }
}
