package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankVerlagAuthenticatorTest {
    private BankverlagAuthenticator authenticator;
    private BankverlagApiClient apiClient;
    private SupplementalInformationController supplementalInformationController;
    private Credentials credentials;
    private BankverlagStorage storage;

    private final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankverlag/resources";

    @Captor ArgumentCaptor<Field> fieldCaptor;

    @Before
    public void init() {
        apiClient = mock(BankverlagApiClient.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        storage = new BankverlagStorage(new PersistentStorage(), new SessionStorage());
        credentials = mock(Credentials.class);
        when(credentials.getField(Key.USERNAME)).thenReturn("dummyUsername");
        when(credentials.getField(Key.PASSWORD)).thenReturn("dummyPassword");
        when(credentials.getType()).thenReturn(CredentialsTypes.PASSWORD);
        authenticator =
                new BankverlagAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        storage,
                        credentials,
                        mock(Catalog.class),
                        "dummyAspsId",
                        "dummyAspspName");
        fieldCaptor = ArgumentCaptor.forClass(Field.class);
    }

    @Test
    public void authenticateShouldRouteAuthenticationToDecoupledIfNoChosenScaMethodInResponse() {
        // given
        ConsentResponse consentResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "consent_response.json").toFile(),
                        ConsentResponse.class);

        when(apiClient.createConsent()).thenReturn(consentResponse);

        AuthorizationResponse initializeAuthorizationResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "authorization_response_only_decoupled.json")
                                .toFile(),
                        AuthorizationResponse.class);
        when(apiClient.initializeAuthorization(
                        consentResponse.getLinks().getStartAuthorisationWithPsuAuthentication(),
                        credentials.getField(Key.USERNAME),
                        credentials.getField(Key.PASSWORD)))
                .thenReturn(initializeAuthorizationResponse);
        storage.savePushOtpFromHeader();

        when(apiClient.getAuthorizationStatus(
                        initializeAuthorizationResponse.getLinks().getScaStatus()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "sca_status_finalised.json").toFile(),
                                AuthorizationResponse.class));

        when(apiClient.getConsentDetails(consentResponse.getConsentId()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_details_response.json").toFile(),
                                ConsentDetailsResponse.class));

        // when
        authenticator.authenticate(credentials);

        // then
        verify(apiClient).createConsent();
        verify(apiClient)
                .initializeAuthorization(
                        consentResponse.getLinks().getStartAuthorisationWithPsuAuthentication(),
                        credentials.getField(Key.USERNAME),
                        credentials.getField(Key.PASSWORD));
        verify(apiClient)
                .getAuthorizationStatus(initializeAuthorizationResponse.getLinks().getScaStatus());
        verify(apiClient).getConsentDetails(consentResponse.getConsentId());
        verifyNoMoreInteractions(apiClient);

        verify(supplementalInformationController)
                .askSupplementalInformationSync(fieldCaptor.capture());
        assertThat(fieldCaptor.getAllValues().get(0).getAdditionalInfo())
                .isEqualTo("{\"layoutType\":\"INSTRUCTIONS\"}");
        assertThat(fieldCaptor.getAllValues().get(0).getName()).isEqualTo("instructionField");
    }
}
