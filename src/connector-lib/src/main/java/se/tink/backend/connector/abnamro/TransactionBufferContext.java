package se.tink.backend.connector.abnamro;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.connector.rpc.abnamro.TransactionAccountEntity;
import se.tink.backend.connector.rpc.abnamro.TransactionEntity;
import se.tink.backend.core.Credentials;
import se.tink.backend.utils.LogUtils;

public class TransactionBufferContext {

    private static final LogUtils log = new LogUtils(TransactionBufferContext.class);

    private TransactionAccountEntity accountWithTransactions;
    private Set<Long> accountNumbers;
    private Map<Long, Integer> bufferedCountByAccountNumber;
    private Set<Long> completeAccounts;
    private Credentials credentials;
    private ListMultimap<Long, TransactionEntity> transactions = ArrayListMultimap.create();

    public TransactionAccountEntity getAccountWithTransactions() {
        return accountWithTransactions;
    }

    public long getBufferedCount() {
        long count = 0;
        
        if (bufferedCountByAccountNumber != null) {
            for (long value : bufferedCountByAccountNumber.values()) {
                count += value;
            }
        }
        
        return count;
    }
    
    public int getBufferedCountForEnabledAccounts() {
        int count = 0;

        if (bufferedCountByAccountNumber != null) {
            for (Long accountNumber : accountNumbers) {
                if (bufferedCountByAccountNumber.containsKey(accountNumber)) {
                    count += bufferedCountByAccountNumber.get(accountNumber);
                }
            }
        }
        
        return count;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public ListMultimap<Long, TransactionEntity> getTransactions() {
        return transactions;
    }
    
    public boolean isBufferComplete() {
        if (completeAccounts == null || completeAccounts.isEmpty()) {
            log.trace("Buffer is not complete because 'completeAccounts' is null or empty.");
            return false;
        }

        for (Long accountNumber : accountNumbers) {
            log.trace(String.format("%d complete: %s", accountNumber, completeAccounts.contains(accountNumber)));
        }

        return completeAccounts.containsAll(accountNumbers);
    }

    public void setAccountsNumbers(Set<Long> accountNumbers) {
        this.accountNumbers = accountNumbers;
    }
    
    public void setAccountWithTransactions(TransactionAccountEntity accountWithTransactions) {
        this.accountWithTransactions = accountWithTransactions;
    }
    
    public void setBufferedCountByAccountNumber(Map<Long, Integer> bufferedCountByAccountNumber) {
        this.bufferedCountByAccountNumber = bufferedCountByAccountNumber;
    }

    public void setCompleteAccounts(Set<Long> completeAccounts) {
        this.completeAccounts = completeAccounts;
    }
    
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void addTransaction(Long accountNumber, TransactionEntity transaction) {
        this.transactions.put(accountNumber, transaction);
    }
    
    public void addTransactions(Long accountNumber, List<TransactionEntity> transactions) {
        this.transactions.putAll(accountNumber, transactions);
    }
}
