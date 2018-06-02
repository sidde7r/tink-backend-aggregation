package se.tink.backend.connector.configuration;

import com.google.inject.AbstractModule;
import se.tink.backend.connector.util.handler.AccountHandler;
import se.tink.backend.connector.util.handler.BalanceHandler;
import se.tink.backend.connector.util.handler.CredentialsHandler;
import se.tink.backend.connector.util.handler.DefaultAccountHandler;
import se.tink.backend.connector.util.handler.DefaultBalanceHandler;
import se.tink.backend.connector.util.handler.DefaultCredentialsHandler;
import se.tink.backend.connector.util.handler.DefaultNotificationHandler;
import se.tink.backend.connector.util.handler.DefaultTransactionHandler;
import se.tink.backend.connector.util.handler.DefaultUserHandler;
import se.tink.backend.connector.util.handler.NotificationHandler;
import se.tink.backend.connector.util.handler.TransactionHandler;
import se.tink.backend.connector.util.handler.UserHandler;

public class ConnectorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserHandler.class).to(DefaultUserHandler.class);
        bind(CredentialsHandler.class).to(DefaultCredentialsHandler.class);
        bind(NotificationHandler.class).to(DefaultNotificationHandler.class);
        bind(TransactionHandler.class).to(DefaultTransactionHandler.class);
        bind(AccountHandler.class).to(DefaultAccountHandler.class);
        bind(BalanceHandler.class).to(DefaultBalanceHandler.class);
    }
}
