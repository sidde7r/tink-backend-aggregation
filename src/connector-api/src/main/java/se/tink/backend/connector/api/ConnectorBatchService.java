package se.tink.backend.connector.api;

import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.rpc.CreateTransactionAccountContainer;
import se.tink.backend.connector.rpc.CreateTransactionBatch;
import se.tink.backend.connector.rpc.DeleteTransactionAccountsContainer;
import se.tink.backend.connector.rpc.TransactionBatchResponse;
import se.tink.backend.connector.rpc.UpdateTransactionAccountContainer;

public interface ConnectorBatchService {

    TransactionBatchResponse ingestTransactionsBatch(final CreateTransactionBatch batch) throws RequestException;
}
