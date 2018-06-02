package se.tink.backend.connector.api;

import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.CreateTransactionAccountContainer;
import se.tink.backend.connector.rpc.DeleteTransactionAccountsContainer;
import se.tink.backend.connector.rpc.UpdateTransactionAccountContainer;

public interface ConnectorTransactionService {

    void ingestTransactions(String externalUserId, final CreateTransactionAccountContainer entity) throws RequestException;

    void updateTransactions(String externalUserId, String externalTransactionId, final UpdateTransactionAccountContainer entity) throws RequestException;

    void deleteTransactions(String externalUserId, final DeleteTransactionAccountsContainer entity) throws RequestException;
}
