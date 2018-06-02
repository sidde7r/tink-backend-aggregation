package se.tink.backend.connector.util.handler;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.config.ConnectorConfiguration;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.connector.util.helper.RepositoryHelper;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.libraries.auth.encryption.PasswordHash;

public class DefaultUserHandler implements UserHandler {
    private RepositoryHelper repositoryHelper;
    private NotificationHandler notificationHandler;
    private SystemServiceFactory systemServiceFactory;
    private AuthenticationConfiguration authenticationConfiguration;
    private FlagsConfiguration flagsConfiguration;

    @Inject
    public DefaultUserHandler(RepositoryHelper repositoryHelper, NotificationHandler notificationHandler,
            SystemServiceFactory systemServiceFactory, AuthenticationConfiguration authenticationConfiguration,
            ConnectorConfiguration connectorConfiguration) {
        this.repositoryHelper = repositoryHelper;
        this.notificationHandler = notificationHandler;
        this.systemServiceFactory = systemServiceFactory;
        this.authenticationConfiguration = authenticationConfiguration;
        this.flagsConfiguration = connectorConfiguration.getFlags();
    }

    @Override
    public User findUser(String externalUserId) throws RequestException {
        Optional<User> user = repositoryHelper.getUser(externalUserId);

        if (!user.isPresent()) {
            throw RequestError.USER_NOT_FOUND.exception().withExternalUserId(externalUserId);
        }

        return user.get();
    }

    @Override
    public User mapToTinkModel(UserEntity userEntity, Market market) throws RequestException {
        if (repositoryHelper.getUser(userEntity.getExternalId()).isPresent()) {
            throw RequestError.USER_ALREADY_EXISTS.exception();
        }
        User user = new User();

        UserProfile userProfile = UserProfile.createDefault(market);
        userProfile.setLocale(userEntity.getLocale());
        user.setProfile(userProfile);
        userProfile.setNotificationSettings(notificationHandler.getSettings());
        user.setUsername(userEntity.getExternalId());
        user.setHash(createPasswordHash(userEntity.getToken()));
        user.setCreated(new Date());
        user.setFlags(getFeatureFlags(market));
        if (userEntity.isBlocked() != null) {
            user.setBlocked(userEntity.isBlocked());
        }

        return user;
    }

    @Override
    public void storeUser(User user) {
        repositoryHelper.createUser(user);
    }

    @Override
    public void deleteUser(User user) {
        DeleteUserRequest deleteUserRequest = new DeleteUserRequest();
        deleteUserRequest.setUserId(user.getId());

        systemServiceFactory.getUpdateService().deleteUser(deleteUserRequest);
    }

    @Override
    public void updateUser(User user, UserEntity userEntity) {
        user.setHash(createPasswordHash(userEntity.getToken()));
        if (userEntity.isBlocked() != null) {
            user.setBlocked(userEntity.isBlocked());
        }
        repositoryHelper.saveUser(user);
    }

    private String createPasswordHash(String token) {
        return PasswordHash.create(token, authenticationConfiguration.getUserPasswordHashAlgorithm());
    }

    private List<String> getFeatureFlags(Market market) {
        if (flagsConfiguration == null) {
            return Collections.emptyList();
        }
        Map<String, Map<String, Double>> flagsByMarket = flagsConfiguration.getRegister();

        if (flagsByMarket == null || flagsByMarket.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> flagsToAdd = Lists.newArrayList();

        // Add market specific flags.
        if (market != null) {
            Map<String, Double> marketFlags = flagsByMarket.get(market.getCodeAsString());
            if (marketFlags != null) {
                flagsToAdd.addAll(marketFlags.keySet());
            }
        }

        // Add general flags.
        Map<String, Double> generalFlags = flagsByMarket.get("ALL");
        if (generalFlags != null) {
            flagsToAdd.addAll(generalFlags.keySet());
        }

        return flagsToAdd;
    }
}
