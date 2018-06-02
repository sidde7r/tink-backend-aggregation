package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.authenticator;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSETestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.device.CommitProfileResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.mocks.ResultCaptor;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class HandelsbankenSECardDeviceAuthenticatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Credentials credentials;
    private String userCode;
    private HandelsbankenSEApiClient client;
    private HandelsbankenPersistentStorage persistentStorage;

    @Before
    public void setUp() throws Exception {
        credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, HandelsbankenSETestConfig.USER_NAME);
        credentials.setField(Field.Key.PASSWORD, HandelsbankenSETestConfig.PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);
        client = spy(new HandelsbankenSEApiClient(new TinkHttpClient(null, credentials), new HandelsbankenSEConfiguration()));
        persistentStorage = new HandelsbankenPersistentStorage(new PersistentStorage());
    }

    @Test
    public void itWorks() throws Exception {
        userCode = "Intercept me!";

        //Don't do this without breakpoint after retrieving supplemental information!!!
        authenticate();

        assertThat(persistentStorage.getProfileId(), notNullValue());
        assertThat(persistentStorage.getTfa(credentials), notNullValue());
    }

    @Test
    public void userEntersUnusualCode() throws Exception {
        userCode = "Wrong";

        this.exception.expect(LoginError.INCORRECT_CREDENTIALS.exception().getClass());

        ResultCaptor<CommitProfileResponse> resultCaptor = captureCommitProfile();

        try {
            authenticate();
        } finally {
            assertEquals("101", resultCaptor.getActual().getCode());
        }

    }

    @Test
    public void userEntersUnknownCode() throws Exception {
        userCode = "123456789";

        this.exception.expect(LoginError.INCORRECT_CREDENTIALS.exception().getClass());

        ResultCaptor<CommitProfileResponse> resultCaptor = captureCommitProfile();

        try {
            authenticate();
        } finally {
            assertEquals("101", resultCaptor.getActual().getCode());
        }

    }

    private ResultCaptor<CommitProfileResponse> captureCommitProfile() {
        ResultCaptor<CommitProfileResponse> resultCaptor = new ResultCaptor();
        doAnswer(resultCaptor).when(client).commitProfile(any());
        return resultCaptor;
    }

    private void authenticate() throws AuthenticationException, AuthorizationException {
        SupplementalInformationController supplementalInformationController = mock(SupplementalInformationController.class);
        when(supplementalInformationController.askSupplementalInformation(any())).thenReturn(ImmutableMap.of("code",
                userCode));
        HandelsbankenSECardDeviceAuthenticator authenticator = new HandelsbankenSECardDeviceAuthenticator(
                client,
                this.persistentStorage,
                supplementalInformationController, new HandelsbankenSEConfiguration());

        authenticator.authenticate(credentials);
    }
}
