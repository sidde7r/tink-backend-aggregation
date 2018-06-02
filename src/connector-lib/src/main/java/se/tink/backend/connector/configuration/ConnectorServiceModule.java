package se.tink.backend.connector.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.connector.api.ConnectorAccountService;
import se.tink.backend.connector.api.ConnectorBatchService;
import se.tink.backend.connector.api.ConnectorTransactionService;
import se.tink.backend.connector.api.ConnectorUserService;
import se.tink.backend.connector.api.ConnectorWebhookService;
import se.tink.backend.connector.controller.ConnectorAccountServiceController;
import se.tink.backend.connector.controller.ConnectorTransactionServiceController;
import se.tink.backend.connector.controller.ConnectorUserServiceController;
import se.tink.backend.connector.controller.ConnectorWebhookServiceController;
import se.tink.backend.connector.transport.ConnectorAccountServiceJerseyTransport;
import se.tink.backend.connector.transport.ConnectorBatchServiceJerseyTransport;
import se.tink.backend.connector.transport.ConnectorTransactionServiceJerseyTransport;
import se.tink.backend.connector.transport.ConnectorUserServiceJerseyTransport;
import se.tink.backend.connector.transport.ConnectorWebhookServiceJerseyTransport;

public class ConnectorServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ConnectorUserServiceController.class).in(Scopes.SINGLETON);
        bind(ConnectorAccountServiceController.class).in(Scopes.SINGLETON);
        bind(ConnectorWebhookServiceController.class).in(Scopes.SINGLETON);
        bind(ConnectorTransactionServiceController.class).in(Scopes.SINGLETON);

        bind(ConnectorUserService.class).to(ConnectorUserServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(ConnectorAccountService.class).to(ConnectorAccountServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(ConnectorWebhookService.class).to(ConnectorWebhookServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(ConnectorBatchService.class).to(ConnectorBatchServiceJerseyTransport.class).in(Scopes.SINGLETON);
        bind(ConnectorTransactionService.class).to(ConnectorTransactionServiceJerseyTransport.class)
                .in(Scopes.SINGLETON);
    }
}
