package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class FabricEmbeddedAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fabric/authenticator/resources/";

    private static final String USERNAME = StringUtils.repeat("A", 30);
    private static final String PASSWORD = "password";

    private FabricEmbeddedAuthenticator fabricEmbeddedAuthenticator;

    private FabricAuthApiClient fabricAuthApiClient;
    private Credentials credentials;

    @Before
    public void setup() {
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");

        FabricSupplementalInformationCollector fabricSupplementalInformationCollector =
                mock(FabricSupplementalInformationCollector.class);

        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        fabricAuthApiClient = mock(FabricAuthApiClient.class);

        credentials = new Credentials();
        credentials.setField(Key.USERNAME, USERNAME);
        credentials.setField(Key.PASSWORD, PASSWORD);

        fabricEmbeddedAuthenticator =
                new FabricEmbeddedAuthenticator(
                        persistentStorage,
                        fabricAuthApiClient,
                        fabricSupplementalInformationCollector,
                        new ConstantLocalDateTimeSource());
    }

    @Test
    public void authenticateShouldThrowExceptionIfUserCredentialsAreInvalid() {
        // given
        when(fabricAuthApiClient.createConsentForEmbeddedFlow(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consentCreated.json").toFile(),
                                ConsentResponse.class));
        when(fabricAuthApiClient.createAuthorizationObject(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "authorisationCreatedResponse.json")
                                        .toFile(),
                                AuthorizationResponse.class));
        when(fabricAuthApiClient.updateAuthorizationWithLoginDetails(
                        "/v1/consents/139407/authorisations/217980", USERNAME, PASSWORD))
                .thenThrow(LoginError.INCORRECT_CREDENTIALS.exception());

        // when
        Throwable t = catchThrowable(() -> fabricEmbeddedAuthenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void authenticateShouldThrowExceptionIfNotSupportedScaMethodIsChosen() {
        // given
        when(fabricAuthApiClient.createConsentForEmbeddedFlow(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consentCreated.json").toFile(),
                                ConsentResponse.class));
        when(fabricAuthApiClient.createAuthorizationObject(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "authorisationCreatedResponse.json")
                                        .toFile(),
                                AuthorizationResponse.class));
        when(fabricAuthApiClient.updateAuthorizationWithLoginDetails(
                        "/v1/consents/139407/authorisations/217980", USERNAME, PASSWORD))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "authorisationResponseWithNotSupportedSca.json")
                                        .toFile(),
                                AuthorizationResponse.class));

        // when
        Throwable t = catchThrowable(() -> fabricEmbeddedAuthenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NO_AVAILABLE_SCA_METHODS");
    }

    @Test
    public void authenticateShouldThrowExceptionIfNoSupportedScaMethodOnAList() {
        // given
        when(fabricAuthApiClient.createConsentForEmbeddedFlow(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consentCreated.json").toFile(),
                                ConsentResponse.class));
        when(fabricAuthApiClient.createAuthorizationObject(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "authorisationCreatedResponse.json")
                                        .toFile(),
                                AuthorizationResponse.class));
        when(fabricAuthApiClient.updateAuthorizationWithLoginDetails(
                        "/v1/consents/139407/authorisations/217980", USERNAME, PASSWORD))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "authorisationResponseWithMutlipleNotSupportedSca.json")
                                        .toFile(),
                                AuthorizationResponse.class));

        // when
        Throwable t = catchThrowable(() -> fabricEmbeddedAuthenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NO_AVAILABLE_SCA_METHODS");
    }
}
