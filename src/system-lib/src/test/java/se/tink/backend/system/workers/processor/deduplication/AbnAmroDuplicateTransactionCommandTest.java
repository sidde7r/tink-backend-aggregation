package se.tink.backend.system.workers.processor.deduplication;

import org.junit.Test;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroDuplicateTransactionCommandTest {

    @Test
    public void shouldContinueOnSingleTransaction() {

        AbnAmroDuplicateTransactionCommand command = new AbnAmroDuplicateTransactionCommand();

        Transaction transaction1 = createTransaction("externalId1", "accountId1");

        assertThat(command.execute(transaction1)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
    }

    @Test
    public void shouldContinueOnTransactionsWithDifferentExternalIds() {

        AbnAmroDuplicateTransactionCommand command = new AbnAmroDuplicateTransactionCommand();

        Transaction transaction1 = createTransaction("externalId1", "accountId1");
        Transaction transaction2 = createTransaction("externalId2", "accountId1");

        assertThat(command.execute(transaction1)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
        assertThat(command.execute(transaction2)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
    }

    @Test
    public void shouldContinueOnTransactionsOnAccounts() {

        AbnAmroDuplicateTransactionCommand command = new AbnAmroDuplicateTransactionCommand();

        Transaction transaction1 = createTransaction("externalId1", "accountId1");
        Transaction transaction2 = createTransaction("externalId1", "accountId2");

        assertThat(command.execute(transaction1)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
        assertThat(command.execute(transaction2)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
    }

    @Test
    public void shouldBreakOnOneDuplicate() {

        AbnAmroDuplicateTransactionCommand command = new AbnAmroDuplicateTransactionCommand();

        Transaction transaction1 = createTransaction("externalId1", "accountId1");
        Transaction transaction2 = createTransaction("externalId1", "accountId1");

        assertThat(command.execute(transaction1)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
        assertThat(command.execute(transaction2)).isEqualTo(TransactionProcessorCommandResult.BREAK);
    }

    @Test
    public void shouldBreakOnTwoDuplicate() {

        AbnAmroDuplicateTransactionCommand command = new AbnAmroDuplicateTransactionCommand();

        Transaction transaction1 = createTransaction("externalId1", "accountId1");
        Transaction transaction2 = createTransaction("externalId1", "accountId1");
        Transaction transaction3 = createTransaction("externalId1", "accountId1");

        assertThat(command.execute(transaction1)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
        assertThat(command.execute(transaction2)).isEqualTo(TransactionProcessorCommandResult.BREAK);
        assertThat(command.execute(transaction3)).isEqualTo(TransactionProcessorCommandResult.BREAK);
    }

    @Test
    public void shouldContinueForTransactionsWithoutExternalIdDuplicate() {

        AbnAmroDuplicateTransactionCommand command = new AbnAmroDuplicateTransactionCommand();

        Transaction transaction1 = createTransaction(null, "accountId1");
        Transaction transaction2 = createTransaction("", "accountId1");

        assertThat(command.execute(transaction1)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
        assertThat(command.execute(transaction2)).isEqualTo(TransactionProcessorCommandResult.CONTINUE);
    }

    private Transaction createTransaction(String externalId, String accountId) {
        Transaction transaction = new Transaction();

        transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, externalId);
        transaction.setAccountId(accountId);
        transaction.setUserId("userId");

        return transaction;
    }
}
