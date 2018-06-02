package se.tink.backend.connector.controller;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.connector.util.handler.CredentialsHandler;
import se.tink.backend.connector.util.handler.UserHandler;
import se.tink.backend.connector.util.helper.LogHelper;
import se.tink.backend.connector.util.helper.RepositoryHelper;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class ConnectorUserServiceController {

    private Supplier<List<Market>> marketSupplier;
    private UserHandler userHandler;
    private CredentialsHandler credentialsHandler;

    private static final LogUtils log = new LogUtils(ConnectorUserServiceController.class);

    @Inject
    public ConnectorUserServiceController(UserHandler userHandler, CredentialsHandler credentialsHandler,
            RepositoryHelper repositoryHelper) {
        this.userHandler = userHandler;
        this.credentialsHandler = credentialsHandler;
        this.marketSupplier = Suppliers.memoizeWithExpiration(
                () -> Collections.unmodifiableList(repositoryHelper.getAllMarkets()), 30, TimeUnit.MINUTES);
    }

    @Timed
    public void createUser(UserEntity userEntity) throws RequestException {
        String marketCode = userEntity.getMarket() == null ? Market.Code.SE.name() : userEntity.getMarket().name();
        Market market = marketSupplier.get().stream()
                .filter(m -> Objects.equals(marketCode, m.getCodeAsString()))
                .findFirst().orElse(null);

        if (userEntity.getLocale() == null) {
            userEntity.setLocale(market.getDefaultLocale());
        }

        if (market == null) {
            throw RequestError.MARKET_NOT_FOUND.exception().withExternalUserId(userEntity.getExternalId());
        }

        User user = userHandler.mapToTinkModel(userEntity, market);
        userHandler.storeUser(user);

        Credentials credentials = credentialsHandler.createCredentials(user);
        credentialsHandler.storeCredentials(credentials);

        log.info(credentials, "Created user: " + LogHelper.get(user));
    }

    public void deleteUser(String externalUserId) throws RequestException {
        User user = userHandler.findUser(externalUserId);

        log.info(user.getId(), "Deleting user: " + LogHelper.get(user));

        userHandler.deleteUser(user);
    }

    public void updateUser(String externalUserId, UserEntity entity) throws RequestException {
        User user = userHandler.findUser(externalUserId);

        log.info(user.getId(), "Updating user: " + LogHelper.get(user));

        userHandler.updateUser(user, entity);
    }
}
