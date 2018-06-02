package se.tink.backend.connector.util.handler;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.config.ConnectorConfiguration;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.connector.util.helper.RepositoryHelper;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.libraries.auth.encryption.HashingAlgorithm;

import static org.junit.Assert.*;

public class DefaultUserHandlerTest {

    private RepositoryHelper repositoryHelper;
    private NotificationHandler notificationHandler;
    private SystemServiceFactory systemServiceFactory;
    private AuthenticationConfiguration authenticationConfiguration;
    private ConnectorConfiguration connectorConfiguration;
    private FlagsConfiguration flagsConfiguration;
    private String externalId = "someExternalId";
    private String token = "someToken";
    private String allMarket = "ALL";
    private UserEntity entity;

    private Map<String, Map<String, Double>> createRegister(String market) {
        Map<String, Double> flags = new HashMap<>();
        Map<String, Map<String, Double>> register = new HashMap<>();
        flags.put(FeatureFlags.TINK_TEST_ACCOUNT, 1.0);
        register.put(market, flags);
        return register;
    }

    @Before
    public void setUp() {
        repositoryHelper = Mockito.mock(RepositoryHelper.class);
        notificationHandler = Mockito.mock(NotificationHandler.class);
        systemServiceFactory = Mockito.mock(SystemServiceFactory.class);
        authenticationConfiguration = Mockito.mock(AuthenticationConfiguration.class);
        connectorConfiguration = Mockito.mock(ConnectorConfiguration.class);
        flagsConfiguration = Mockito.mock(FlagsConfiguration.class);
        Mockito.when(authenticationConfiguration.getUserPasswordHashAlgorithm()).thenReturn(HashingAlgorithm.BCRYPT);

        entity = new UserEntity();
        entity.setExternalId(externalId);
        entity.setToken(token);

        Map<String, Map<String, Double>> allRegister = createRegister(allMarket);
        Mockito.when(flagsConfiguration.getRegister()).thenReturn(allRegister);
        Mockito.when(connectorConfiguration.getFlags()).thenReturn(flagsConfiguration);
    }

    @Test
    public void testUserFlagCreation() throws RequestException {
        UserHandler userHandler = new DefaultUserHandler(repositoryHelper, notificationHandler, systemServiceFactory,
                authenticationConfiguration, connectorConfiguration);
        Market market = new Market();
        market.setCode(allMarket);
        User user = userHandler.mapToTinkModel(entity, market);
        assertTrue(user.getFlags().size() > 0);
        assertTrue(user.getFlags().contains(FeatureFlags.TINK_TEST_ACCOUNT));
    }

    @Test
    public void testIsBlockedIsUnchangedFalseIfNull() {
        UserHandler userHandler = new DefaultUserHandler(repositoryHelper, notificationHandler, systemServiceFactory,
                authenticationConfiguration, connectorConfiguration);

        User user = new User();
        user.setBlocked(false);

        userHandler.updateUser(user, entity);

        assertFalse(user.isBlocked());
    }

    @Test
    public void testIsBlockedIsUnchangedTrueIfNull() {
        UserHandler userHandler = new DefaultUserHandler(repositoryHelper, notificationHandler, systemServiceFactory,
                authenticationConfiguration, connectorConfiguration);

        User user = new User();
        user.setBlocked(true);

        userHandler.updateUser(user, entity);

        assertTrue(user.isBlocked());
    }

    @Test
    public void testIsBlockedIsSetTrueIfTrue() {
        UserHandler userHandler = new DefaultUserHandler(repositoryHelper, notificationHandler, systemServiceFactory,
                authenticationConfiguration, connectorConfiguration);

        User user = new User();
        user.setBlocked(false);
        entity.setBlocked(true);

        userHandler.updateUser(user, entity);

        assertTrue(user.isBlocked());
    }

    @Test
    public void testIsBlockedIsSetFalseIfFalse() {
        UserHandler userHandler = new DefaultUserHandler(repositoryHelper, notificationHandler, systemServiceFactory,
                authenticationConfiguration, connectorConfiguration);

        User user = new User();
        user.setBlocked(true);
        entity.setBlocked(false);

        userHandler.updateUser(user, entity);

        assertFalse(user.isBlocked());
    }

}
