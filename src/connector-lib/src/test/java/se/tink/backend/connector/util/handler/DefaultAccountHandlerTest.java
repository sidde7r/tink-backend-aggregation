package se.tink.backend.connector.util.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.connector.rpc.CreateTransactionAccountEntity;
import se.tink.backend.connector.rpc.PartnerAccountPayload;
import se.tink.backend.connector.rpc.TransactionAccountEntity;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultAccountHandlerTest {

    @Test
    public void calculateBalanceOnAccount_andOnIngest_withHistoricalType_calculatesBalanceFromTransactionAmounts() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, null, balanceHandler);

        // We have an account in store with CALCULATE_BALANCE set and balance = 0.
        Account accountInStore = new Account();
        setCalculateBalanceOnAccount(accountInStore);

        // We're ingesting transactions with the CALCULATE_BALANCE flag.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        setCalculateBalanceOnTransactionAccount(transactionAccount);

        List<Transaction> transactions = Lists
                .newArrayList(createTransaction(-149.43, false), createTransaction(5284, false));

        // HISTORICAL type is OK.
        accountHandler.updateAccount(TransactionContainerType.HISTORICAL, accountInStore, transactions,
                transactionAccount, CRUDType.CREATE);

        // Make sure the balance was calculated according to the transaction amounts.
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(accountRepositoryMock, times(1)).addToBalanceById(any(), captor.capture());
        assertEquals(5284 - 149.43, captor.getValue(), 0);
    }

    @Test
    public void calculateBalanceOnAccount_andOnIngest_withRealTimeType_calculatesBalanceFromTransactionAmounts() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, null, balanceHandler);

        // We have an account in store with CALCULATE_BALANCE set and balance = 0.
        Account accountInStore = new Account();
        setCalculateBalanceOnAccount(accountInStore);

        // We're ingesting transactions with the CALCULATE_BALANCE flag.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        setCalculateBalanceOnTransactionAccount(transactionAccount);

        List<Transaction> transactions = Lists
                .newArrayList(createTransaction(-200.0, false), createTransaction(1121.1, false));

        // REAL_TIME type is OK.
        accountHandler.updateAccount(TransactionContainerType.HISTORICAL, accountInStore, transactions,
                transactionAccount, CRUDType.CREATE);

        // Make sure the balance was calculated according to the transaction amounts.
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(accountRepositoryMock, times(1)).addToBalanceById(any(), captor.capture());
        assertEquals(1121.1 - 200.0, captor.getValue(), 0);
    }

    @Test
    public void calculateBalanceOnAccount_andOnIngest_withSetBalance_calculatesBalanceFromTransactionAmounts() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, null, balanceHandler);

        // We have an account in store with CALCULATE_BALANCE set and balance = 0.
        Account accountInStore = new Account();
        setCalculateBalanceOnAccount(accountInStore);

        // We're ingesting transactions with the CALCULATE_BALANCE flag.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        setCalculateBalanceOnTransactionAccount(transactionAccount);

        // We set the balance and the reserved amount, but these should be ignored.
        transactionAccount.setBalance(14572.8);
        transactionAccount.setReservedAmount(944.2);

        List<Transaction> transactions = Lists
                .newArrayList(createTransaction(-200.0, false), createTransaction(1121.1, false));
        accountHandler.updateAccount(TransactionContainerType.HISTORICAL, accountInStore, transactions,
                transactionAccount, CRUDType.CREATE);

        // Make sure the balance was calculated according to the transaction amounts.
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(accountRepositoryMock, times(1)).addToBalanceById(any(), captor.capture());
        assertEquals(1121.1 - 200.0, captor.getValue(), 0);
    }

    @Test
    public void calculateBalanceOnAccount_andOnIngest_withPendingTrx_calculatesBalanceFromNonPendingTransactions() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, null, balanceHandler);

        // We have an account in store with CALCULATE_BALANCE set and balance = 0.
        Account accountInStore = new Account();
        setCalculateBalanceOnAccount(accountInStore);

        // We're ingesting transactions with the CALCULATE_BALANCE flag.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        setCalculateBalanceOnTransactionAccount(transactionAccount);

        // One pending transaction and two non-pending.
        List<Transaction> transactions = Lists.newArrayList(createTransaction(-149.43, true),
                createTransaction(5284, false), createTransaction(-445.6, false));

        accountHandler.updateAccount(TransactionContainerType.REAL_TIME, accountInStore, transactions,
                transactionAccount, CRUDType.CREATE);

        // Make sure the balance was calculated according to the non-pending transaction amounts.
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(accountRepositoryMock, times(1)).addToBalanceById(any(), captor.capture());
        assertEquals(5284 - 445.6, captor.getValue(), 0);
    }

    @Test
    public void noCalculateBalanceOnAccountInStore_butOnIngest_doesNotCalculateBalance() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        SystemServiceFactory systemServiceFactoryMock = mock(SystemServiceFactory.class);
        UpdateService updateServiceMock = mock(UpdateService.class);
        when(systemServiceFactoryMock.getUpdateService()).thenReturn(updateServiceMock);
        when(updateServiceMock.updateAccount(any())).thenReturn(null);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, systemServiceFactoryMock, balanceHandler);

        Account accountInStore = new Account();

        // We're ingesting transactions with the CALCULATE_BALANCE flag and with a balance and reserved amount.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        setCalculateBalanceOnTransactionAccount(transactionAccount);
        transactionAccount.setBalance(14572.8);
        transactionAccount.setReservedAmount(944.2);

        List<Transaction> transactions = Lists
                .newArrayList(createTransaction(-149.43, false), createTransaction(5284, false));

        accountHandler.updateAccount(TransactionContainerType.REAL_TIME, accountInStore, transactions,
                transactionAccount, CRUDType.CREATE);

        // Make sure the balance was calculated according to (balance - reservedAmount) and NOT the transaction amounts.
        assertEquals(14572.8 - 944.2, accountInStore.getBalance(), 0);
    }

    @Test
    public void noCalculateBalanceOnAccountInStore_noCalculateBalanceOnIngest_doesNotCalculateBalance() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        SystemServiceFactory systemServiceFactoryMock = mock(SystemServiceFactory.class);
        UpdateService updateServiceMock = mock(UpdateService.class);
        when(systemServiceFactoryMock.getUpdateService()).thenReturn(updateServiceMock);
        when(updateServiceMock.updateAccount(any())).thenReturn(null);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, systemServiceFactoryMock, balanceHandler);

        Account accountInStore = new Account();

        // We're ingesting transactions with a balance and reserved amount.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        transactionAccount.setBalance(14572.8);
        transactionAccount.setReservedAmount(944.2);

        List<Transaction> transactions = Lists
                .newArrayList(createTransaction(-149.43, false), createTransaction(5284, false));

        accountHandler.updateAccount(TransactionContainerType.REAL_TIME, accountInStore, transactions,
                transactionAccount, CRUDType.CREATE);

        // Make sure the balance was calculated according to (balance - reservedAmount) and NOT the transaction amounts.
        assertEquals(14572.8 - 944.2, accountInStore.getBalance(), 0);
    }

    @Test
    public void noCalculateBalanceOnAccountInStore_noCalculateBalanceOnIngest_historicalType_ignoresSetBalance() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, null, balanceHandler);

        Account accountInStore = new Account();
        accountInStore.setBalance(3456.1);

        // We're ingesting transactions with a balance and reserved amount.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        transactionAccount.setBalance(14572.8);
        transactionAccount.setReservedAmount(944.2);

        List<Transaction> transactions = Lists
                .newArrayList(createTransaction(-149.43, false), createTransaction(5284, false));

        accountHandler.updateAccount(TransactionContainerType.HISTORICAL, accountInStore, transactions,
                transactionAccount, CRUDType.CREATE);

        // Make sure the balance and reservedAmount values were ignored since this is a HISTORICAL batch.
        assertEquals(3456.1, accountInStore.getBalance(), 0);
    }

    @Test
    public void calculateBalanceOnAccount_andOnUpdate_updatesBalanceWithNewTransactionAmount() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, null, balanceHandler);

        // We have an account in store with CALCULATE_BALANCE set and balance = 0.
        Account accountInStore = new Account();
        setCalculateBalanceOnAccount(accountInStore);

        // We're updating a transaction.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        setCalculateBalanceOnTransactionAccount(transactionAccount);

        Transaction newTransaction = createTransaction(-1400, false);
        Transaction oldTransaction = createTransaction(-1500, false);

        List<Transaction> transactions = Lists.newArrayList(newTransaction, oldTransaction);
        accountHandler.updateAccount(TransactionContainerType.REAL_TIME, accountInStore, transactions,
                transactionAccount, CRUDType.UPDATE);

        // Make sure the balance was updated with the new transaction amount.
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(accountRepositoryMock, times(1)).addToBalanceById(any(), captor.capture());
        assertEquals(newTransaction.getOriginalAmount() - oldTransaction.getOriginalAmount(), captor.getValue(), 0);
    }

    @Test
    public void calculateBalanceOnAccount_andOnDelete_RemovesTransactionAmountsFromBalance() {
        AccountRepository accountRepositoryMock = mock(AccountRepository.class);
        BalanceHandler balanceHandler = new DefaultBalanceHandler(accountRepositoryMock);
        AccountHandler accountHandler = new DefaultAccountHandler(null, null, balanceHandler);

        // We have an account in store with CALCULATE_BALANCE set.
        Account accountInStore = new Account();
        setCalculateBalanceOnAccount(accountInStore);

        // We're deleting two transactions.
        TransactionAccountEntity transactionAccount = new CreateTransactionAccountEntity();
        setCalculateBalanceOnTransactionAccount(transactionAccount);

        List<Transaction> transactions = Lists
                .newArrayList(createTransaction(-149.43, false), createTransaction(1337, false));

        accountHandler.updateAccount(TransactionContainerType.REAL_TIME, accountInStore, transactions,
                transactionAccount, CRUDType.DELETE);

        // Make sure the transaction amounts were removed from the balance.
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(accountRepositoryMock, times(1)).addToBalanceById(any(), captor.capture());
        assertEquals(149.43 - 1337, captor.getValue(), 0);
    }

    private void setCalculateBalanceOnAccount(Account accountInStore) {
        PartnerAccountPayload partnerPayload = new PartnerAccountPayload();
        partnerPayload.setCalculateBalance(true);
        accountInStore.putPayload(Account.PayloadKeys.PARTNER_PAYLOAD,
                SerializationUtils.serializeToString(partnerPayload));
    }

    private void setCalculateBalanceOnTransactionAccount(TransactionAccountEntity transactionAccount) {
        Map<String, Object> payload = Maps.newHashMap();
        payload.put(PartnerAccountPayload.CALCULATE_BALANCE, true);
        transactionAccount.setPayload(payload);
    }

    private Transaction createTransaction(double amount, boolean pending) {
        Transaction transaction = new Transaction();
        transaction.setOriginalAmount(amount);
        transaction.setPending(pending);
        return transaction;
    }
}
