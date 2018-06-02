package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFITestConfig;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.SecurityCardResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.rpc.device.VerifySecurityCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
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

public class HandelsbankenFICardDeviceAuthenticatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Credentials credentials;
    private String userCode;
    private HandelsbankenFIApiClient client;
    private HandelsbankenPersistentStorage persistentStorage;
    private HandelsbankenSessionStorage sessionStorage;
    private String userId;
    private String signupPassword;

    @Before
    public void setUp() throws Exception {
        credentials = new Credentials();
        userId = HandelsbankenFITestConfig.USER_1.getUserName();
        signupPassword = HandelsbankenFITestConfig.USER_1.getSignupPassword();
        userCode = "Intercept me!";
        client = spy(
                new HandelsbankenFIApiClient(new TinkHttpClient(null, credentials), new HandelsbankenFIConfiguration()));
        persistentStorage = new HandelsbankenPersistentStorage(new PersistentStorage());
        sessionStorage = new HandelsbankenSessionStorage(new SessionStorage(), new HandelsbankenFIConfiguration());
    }

    @Test
    public void itWorks() throws Exception {
        //Don't do this without breakpoint after retrieving supplemental information!!!
        authenticate();

        // And save the persistent storage for future usage!
        assertThat(persistentStorage.getProfileId(), notNullValue());
        assertThat(persistentStorage.getTfa(credentials), notNullValue());
    }

    @Test
    public void unknownUserId() throws Exception {
        userId = "123456768";

        this.exception.expect(LoginError.INCORRECT_CREDENTIALS.exception().getClass());

        ResultCaptor<SecurityCardResponse> resultCaptor = captureSecurityCardResponse();

        try {
            authenticate();
        } finally {
            assertEquals("101", resultCaptor.getActual().getCode());
        }
    }

    @Test
    public void unknownPassword() throws Exception {
        signupPassword = "1234";

        this.exception.expect(LoginError.INCORRECT_CREDENTIALS.exception().getClass());

        ResultCaptor<SecurityCardResponse> resultCaptor = captureSecurityCardResponse();

        try {
            authenticate();
        } finally {
            assertEquals("102", resultCaptor.getActual().getCode());
        }
    }

    @Test
    public void userEntersUnusualCode() throws Exception {
        userCode = "Wrong";

        this.exception.expect(LoginError.INCORRECT_CREDENTIALS.exception().getClass());

        ResultCaptor<VerifySecurityCodeResponse> resultCaptor = captureVerifySecurityCode();

        try {
            authenticate();
        } finally {
            assertEquals("102", resultCaptor.getActual().getCode());
        }

    }

    @Test
    public void userEntersUnknownCode() throws Exception {
        userCode = "123456789";

        this.exception.expect(LoginError.INCORRECT_CREDENTIALS.exception().getClass());

        ResultCaptor<VerifySecurityCodeResponse> resultCaptor = captureVerifySecurityCode();

        try {
            authenticate();
        } finally {
            assertEquals("102", resultCaptor.getActual().getCode());
        }
    }

    private ResultCaptor<SecurityCardResponse> captureSecurityCardResponse() {
        ResultCaptor<SecurityCardResponse> resultCaptor = new ResultCaptor<>();
        doAnswer(resultCaptor).when(client).authenticate(any(), any());
        return resultCaptor;
    }

    private ResultCaptor<VerifySecurityCodeResponse> captureVerifySecurityCode() {
        ResultCaptor<VerifySecurityCodeResponse> resultCaptor = new ResultCaptor();
        doAnswer(resultCaptor).when(client).verifySecurityCode(any(), any());
        return resultCaptor;
    }

    private void authenticate() throws AuthenticationException, AuthorizationException {
        credentials.setField(Field.Key.USERNAME, userId);
        credentials.setField(HandelsbankenFIConstants.DeviceAuthentication.SIGNUP_PASSWORD, signupPassword);
        SupplementalInformationController supplementalInformationController = mock(
                SupplementalInformationController.class);
        when(supplementalInformationController.askSupplementalInformation(any()))
                .thenReturn(ImmutableMap.of("code", userCode));
        HandelsbankenFICardDeviceAuthenticator authenticator = new HandelsbankenFICardDeviceAuthenticator(
                client,
                this.persistentStorage,
                supplementalInformationController,
                new HandelsbankenFIConfiguration(),
                new HandelsbankenAutoAuthenticator(client, this.persistentStorage,
                        credentials,
                        sessionStorage, new HandelsbankenFIConfiguration()));

        authenticator.authenticate(credentials);
    }
}
