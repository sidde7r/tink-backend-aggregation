package se.tink.backend.connector.controller;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.AccountEntity;
import se.tink.backend.connector.rpc.AccountListEntity;
import se.tink.backend.connector.util.handler.AccountHandler;
import se.tink.backend.connector.util.handler.CredentialsHandler;
import se.tink.backend.connector.util.handler.UserHandler;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;

public class ConnectorAccountServiceController {

    private static final LogUtils log = new LogUtils(ConnectorAccountServiceController.class);

    private final UserHandler userHandler;
    private final CredentialsHandler credentialsHandler;
    private final AccountHandler accountHandler;

    @Inject
    public ConnectorAccountServiceController(UserHandler userHandler, CredentialsHandler credentialsHandler,
            AccountHandler accountHandler) {
        this.userHandler = userHandler;
        this.credentialsHandler = credentialsHandler;
        this.accountHandler = accountHandler;
    }

    @Timed
    public void createAccounts(String externalUserId, AccountListEntity accountListEntity) throws RequestException {
        User user = userHandler.findUser(externalUserId);
        Credentials credentials = credentialsHandler.findCredentials(user, externalUserId);

        log.info(credentials, String.format("Received %d accounts.", accountListEntity.getAccounts().size()));

        for (AccountEntity accountEntity : accountListEntity.getAccounts()) {
            Account account = accountHandler.mapToTinkModel(accountEntity, user, credentials);
            accountHandler.storeAccount(account);
        }
    }

    public void deleteAccount(String externalUserId, String externalAccountId) throws RequestException {
        User user = userHandler.findUser(externalUserId);
        Credentials credentials = credentialsHandler.findCredentials(user, externalUserId);
        Optional<Account> account = accountHandler.findAccounts(user, credentials).stream()
                .filter(a -> Objects.equals(a.getBankId(), externalAccountId)).findFirst();

        if (!account.isPresent()) {
            throw RequestError.ACCOUNT_NOT_FOUND.exception().withExternalUserId(externalUserId)
                    .withExternalAccountId(externalAccountId);
        }

        accountHandler.deleteAccount(account.get(), credentials, user);
    }
}
