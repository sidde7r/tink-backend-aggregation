package se.tink.backend.main.auth.authenticators;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.auth.AuthenticationRequirements;
import se.tink.backend.common.cache.NonCachingCacheClient;
import se.tink.backend.common.config.UserSessionConfiguration;
import se.tink.backend.common.providers.MarketProvider;
import se.tink.backend.common.providers.OAuth2ClientProvider;
import se.tink.backend.common.repository.mysql.UserSessionInMemoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Client;
import se.tink.backend.core.SessionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserSession;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.main.auth.session.UserSessionController;
import se.tink.backend.main.auth.validators.ClientValidator;
import se.tink.backend.main.providers.ClientProvider;
import se.tink.backend.utils.StringUtils;

public class SessionAuthenticatorTest {

    private static final String SESSION_ID1 = StringUtils.generateUUID();
    private static final String SESSION_ID2 = StringUtils.generateUUID();

    private static final String USER_ID1 = StringUtils.generateUUID();
    private static final String USER_ID2 = StringUtils.generateUUID();

    private static final String USERNAME1 = "username1";
    private static final String USERNAME2 = "username2";
    private static final String CLIENT_KEY = "client-key";

    private UserSessionInMemoryRepository repository;
    private UserSessionController userSessionController;
    private UserRepository userRepository;
    private AuthenticationRequirements authenticationRequirements;
    private ClientValidator clientValidator;

    @Before
    public void setUp() {
        repository = new UserSessionInMemoryRepository(Lists.newArrayList(session(SESSION_ID1, USER_ID1)));

        ClientProvider clientProvider = Mockito.mock(ClientProvider.class);
        MarketProvider marketProvider = Mockito.mock(MarketProvider.class);
        UserSessionConfiguration userSessionConfiguration = new UserSessionConfiguration();

        authenticationRequirements = Mockito.mock(AuthenticationRequirements.class);
        userRepository = Mockito.mock(UserRepository.class);
        clientValidator = new ClientValidator(clientProvider, marketProvider);
        userSessionController = new UserSessionController(repository, new NonCachingCacheClient(),
                Mockito.mock(ClientValidator.class), Mockito.mock(OAuth2ClientProvider.class),
                userSessionConfiguration);

        Client client = new Client();
        client.setSessionType(SessionTypes.MOBILE);
        client.setAllowed(true);

        Mockito.when(userRepository.findOne(USER_ID1)).thenReturn(user(USER_ID1, USERNAME1));
        Mockito.when(userRepository.findOne(USER_ID2)).thenReturn(user(USER_ID2, USERNAME2));
        Mockito.when(clientProvider.get()).thenReturn(ImmutableMap.of(CLIENT_KEY, client));
    }

    @Test
    public void verifyNonExistingSessionGivesNoAuthenticatedUser() {
        SessionAuthenticator authenticator = new SessionAuthenticator(
                userRepository, userSessionController, clientValidator);

        AuthenticatedUser authenticatedUser = authenticator
                .authenticate(authenticationRequirements, request(SESSION_ID2));

        Assert.assertNull(authenticatedUser);
    }

    @Test
    public void verifyExistingSessionButNonExistingUserGivesNoAuthenticatedUser() {
        SessionAuthenticator authenticator = new SessionAuthenticator(
                userRepository, userSessionController, clientValidator);

        Mockito.when(userRepository.findOne(USER_ID1)).thenReturn(null);

        AuthenticatedUser authenticatedUser = authenticator
                .authenticate(authenticationRequirements, request(SESSION_ID1));

        Assert.assertNull(authenticatedUser);
    }

    @Test
    public void verifyExistingSessionButNonExistingUserRemovesSession() {
        SessionAuthenticator authenticator = new SessionAuthenticator(
                userRepository, userSessionController, clientValidator);

        Mockito.when(userRepository.findOne(USER_ID1)).thenReturn(null);

        Assert.assertNotNull(repository.findOne(SESSION_ID1));

        authenticator.authenticate(authenticationRequirements, request(SESSION_ID1));

        Assert.assertNull(repository.findOne(SESSION_ID1));
    }

    @Test
    public void verifyExistingSessionAndUserGivesAuthenticatedUser() {
        SessionAuthenticator authenticator = new SessionAuthenticator(
                userRepository, userSessionController, clientValidator);

        AuthenticatedUser authenticatedUser = authenticator
                .authenticate(authenticationRequirements, request(SESSION_ID1));

        Assert.assertEquals(USERNAME1, authenticatedUser.getUser().getUsername());
        Assert.assertEquals(USER_ID1, authenticatedUser.getUser().getId());
    }

    private static UserSession session(String id, String userId) {
        UserSession session = new UserSession();
        session.setId(id);
        session.setUserId(userId);
        return session;
    }

    private static User user(String userId, String username) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        return user;
    }

    private static AuthenticationContextRequest request(String sessionId) {
        AuthenticationContextRequest request = new AuthenticationContextRequest();
        request.setAuthenticationDetails(new AuthenticationDetails(HttpAuthenticationMethod.SESSION, sessionId));
        request.setClientKey(CLIENT_KEY);
        return request;
    }

}
