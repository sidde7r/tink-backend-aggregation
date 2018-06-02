package se.tink.backend.connector.resources;

import java.util.List;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.core.Account;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;

public abstract class ConnectorServiceResource {
    private static final String CONNECTOR_TRANSACTIONS_INCOMING = "connector_transactions_incoming";

    protected final SystemServiceFactory systemServiceFactory;
    final TaskSubmitter taskSubmitter;
    protected final MetricRegistry metricRegistry;

    ConnectorServiceResource(MetricRegistry metricRegistry, SystemServiceFactory systemServiceFactory,
                             TaskSubmitter taskSubmitter) {
        this.metricRegistry = metricRegistry;
        this.systemServiceFactory = systemServiceFactory;
        this.taskSubmitter = taskSubmitter;
    }

    protected void updateAccount(Account account) {
        UpdateAccountRequest updateAccountsRequest = new UpdateAccountRequest();

        updateAccountsRequest.setAccount(account);
        updateAccountsRequest.setAccountFeatures(AccountFeatures.createEmpty());
        updateAccountsRequest.setCredentialsId(account.getCredentialsId());
        updateAccountsRequest.setUser(account.getUserId());

        systemServiceFactory.getUpdateService().updateAccount(updateAccountsRequest);
    }

    protected void updateCredentials(Credentials credentials) {
        UpdateCredentialsStatusRequest updateCredentialsRequest = new UpdateCredentialsStatusRequest();

        updateCredentialsRequest.setCredentials(credentials);
        updateCredentialsRequest.setUpdateContextTimestamp(true);
        updateCredentialsRequest.setUserId(credentials.getUserId());

        systemServiceFactory.getUpdateService().updateCredentials(updateCredentialsRequest);
    }

    void reportTransactionMetric(List<Transaction> transactions, TransactionContainerType containerType) {
        metricRegistry.meter(MetricId.newId(CONNECTOR_TRANSACTIONS_INCOMING).label("origin", containerType.toString()))
                .inc(transactions.size());
    }
}
