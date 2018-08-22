package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseInfoPaymentResponseTest {
    @Test
    public void verifyValidPaymentFromAccountsAreReturned() {
        TransactionAccountEntity firstTransactionAccountEntity = new TransactionAccountEntity();
        TransactionAccountEntity secondTransactionAccountEntity = new TransactionAccountEntity();

        firstTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.PAYMENT_FROM,
                TransactionAccountEntity.AccountScope.TRANSFER_TO));
        secondTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.PAYMENT_FROM));

        BaseInfoPaymentResponse baseInfoPaymentResponse = createBaseInfoPaymentResponseWithTransactionAccountEntities(
                firstTransactionAccountEntity,
                secondTransactionAccountEntity);

        List<TransactionAccountEntity> paymentAccounts = baseInfoPaymentResponse.getPaymentFromAccounts();
        assertThat(paymentAccounts).contains(firstTransactionAccountEntity, paymentAccounts.get(0));
        assertThat(paymentAccounts).contains(secondTransactionAccountEntity, paymentAccounts.get(1));
    }

    @Test
    public void verifyInvalidPaymentFromAccountIsNotReturned() {
        TransactionAccountEntity firstTransactionAccountEntity = new TransactionAccountEntity();
        TransactionAccountEntity secondTransactionAccountEntity = new TransactionAccountEntity();

        firstTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.PAYMENT_FROM));
        secondTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_FROM));

        BaseInfoPaymentResponse baseInfoPaymentResponse = createBaseInfoPaymentResponseWithTransactionAccountEntities(
                firstTransactionAccountEntity,
                secondTransactionAccountEntity);

        List<TransactionAccountEntity> paymentAccounts = baseInfoPaymentResponse.getPaymentFromAccounts();
        assertThat(paymentAccounts).contains(firstTransactionAccountEntity, paymentAccounts.get(0));
        assertThat(paymentAccounts).doesNotContain(secondTransactionAccountEntity);
    }

    @Test
    public void verifyValidTransferFromAccountsAreReturned() {
        TransactionAccountEntity firstTransactionAccountEntity = new TransactionAccountEntity();
        TransactionAccountEntity secondTransactionAccountEntity = new TransactionAccountEntity();

        firstTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_TO,
                TransactionAccountEntity.AccountScope.TRANSFER_FROM));
        secondTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_FROM));

        BaseInfoPaymentResponse baseInfoPaymentResponse = createBaseInfoPaymentResponseWithTransactionAccountEntities(
                firstTransactionAccountEntity,
                secondTransactionAccountEntity);

        List<TransactionAccountEntity> transferFromAccounts = baseInfoPaymentResponse.getTransferFromAccounts();
        assertThat(transferFromAccounts).contains(firstTransactionAccountEntity, transferFromAccounts.get(0));
        assertThat(transferFromAccounts).contains(secondTransactionAccountEntity, transferFromAccounts.get(1));
    }

    @Test
    public void verifyInvalidTransferFromAccountIsNotReturned() {
        TransactionAccountEntity firstTransactionAccountEntity = new TransactionAccountEntity();
        TransactionAccountEntity secondTransactionAccountEntity = new TransactionAccountEntity();

        firstTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_FROM));
        secondTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_TO));

        BaseInfoPaymentResponse baseInfoPaymentResponse = createBaseInfoPaymentResponseWithTransactionAccountEntities(
                firstTransactionAccountEntity,
                secondTransactionAccountEntity);

        List<TransactionAccountEntity> transferFromAccounts = baseInfoPaymentResponse.getTransferFromAccounts();
        assertThat(transferFromAccounts).contains(firstTransactionAccountEntity, transferFromAccounts.get(0));
        assertThat(transferFromAccounts).doesNotContain(secondTransactionAccountEntity);
    }

    @Test
    public void verifyValidRecipientAccountsAreReturned() {
        TransactionAccountEntity firstTransactionAccountEntity = new TransactionAccountEntity();
        TransactionAccountEntity secondTransactionAccountEntity = new TransactionAccountEntity();

        firstTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_TO,
                TransactionAccountEntity.AccountScope.TRANSFER_FROM));
        secondTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_TO));

        BaseInfoPaymentResponse baseInfoPaymentResponse = createBaseInfoPaymentResponseWithTransactionAccountEntities(
                firstTransactionAccountEntity,
                secondTransactionAccountEntity);

        TransactionAccountEntity externalRecipientAccount = new TransactionAccountEntity();
        TransferGroupEntity transferGroupEntity = new TransferGroupEntity();
        transferGroupEntity.setExternalRecipients(Arrays.asList(externalRecipientAccount));
        baseInfoPaymentResponse.setTransfer(transferGroupEntity);

        List<TransactionAccountEntity> recipientAccounts = baseInfoPaymentResponse.getAllRecipientAccounts();
        assertThat(recipientAccounts).contains(firstTransactionAccountEntity, recipientAccounts.get(0));
        assertThat(recipientAccounts).contains(secondTransactionAccountEntity, recipientAccounts.get(1));
        assertThat(recipientAccounts).contains(externalRecipientAccount, recipientAccounts.get(2));

    }

    @Test
    public void verifyNonRecipientAccountIsNotReturned() {
        TransactionAccountEntity firstTransactionAccountEntity = new TransactionAccountEntity();
        TransactionAccountEntity secondTransactionAccountEntity = new TransactionAccountEntity();

        firstTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_FROM));
        secondTransactionAccountEntity.setScopes(Sets.newHashSet(TransactionAccountEntity.AccountScope.TRANSFER_TO));

        BaseInfoPaymentResponse baseInfoPaymentResponse = createBaseInfoPaymentResponseWithTransactionAccountEntities(
                firstTransactionAccountEntity,
                secondTransactionAccountEntity);

        TransactionAccountEntity externalRecipientAccount = new TransactionAccountEntity();
        TransferGroupEntity transferGroupEntity = new TransferGroupEntity();
        transferGroupEntity.setExternalRecipients(Arrays.asList(externalRecipientAccount));
        baseInfoPaymentResponse.setTransfer(transferGroupEntity);

        List<TransactionAccountEntity> recipientAccounts = baseInfoPaymentResponse.getAllRecipientAccounts();
        assertThat(recipientAccounts).doesNotContain(firstTransactionAccountEntity);
        assertThat(recipientAccounts).contains(secondTransactionAccountEntity, recipientAccounts.get(0));
        assertThat(recipientAccounts).contains(externalRecipientAccount, recipientAccounts.get(0));

    }

    private BaseInfoPaymentResponse createBaseInfoPaymentResponseWithTransactionAccountEntities(
            TransactionAccountEntity firstTransactionAccountEntity,
            TransactionAccountEntity secondTransactionAccountEntity
    ) {
        List<TransactionAccountEntity> accounts = Arrays.asList(firstTransactionAccountEntity, secondTransactionAccountEntity);

        TransactionAccountGroupEntity transactionAccountGroups = new TransactionAccountGroupEntity();
        transactionAccountGroups.setAccounts(accounts);

        BaseInfoPaymentResponse baseInfoPaymentResponse = new BaseInfoPaymentResponse();
        baseInfoPaymentResponse.setTransactionAccountGroups(Arrays.asList(transactionAccountGroups));

        return baseInfoPaymentResponse;
    }
}
