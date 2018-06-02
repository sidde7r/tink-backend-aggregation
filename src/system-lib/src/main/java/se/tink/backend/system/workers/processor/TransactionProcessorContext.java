package se.tink.backend.system.workers.processor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;

/**
 * Holds short-lived mutable state related to the TransactionProcessor. Does _not_ hold state that has the same lifetime
 * as the application (such as things stored in the ServiceContext). Use dependency injection (DI) for that. Neither
 * should it hold unmodified state (such as Provider). Use DI for that, too.
 */
public class TransactionProcessorContext {
    private final List<CategoryChangeRecord> categoryChangeRecords;
    private String credentialsId;
    private final List<Transaction> inBatchTransactions;
    private final ImmutableMap<String, Provider> providersByName;

    private final List<Transaction> transactionsToDelete;
    private final HashMap<String, Transaction> transactionsToSave = Maps.newHashMap();
    private final Set<String> transactionsToUpdateList;
    private final User user;
    private TransactionProcessorUserData userData = new TransactionProcessorUserData();

    public TransactionProcessorContext(User user, Map<String, Provider> providersByName,
            List<Transaction> transactions) {
        this(user, providersByName, transactions, null, null);
    }

    public TransactionProcessorContext(User user, Map<String, Provider> providersByName, List<Transaction> transactions,
            UserData data, String credentialsId) {

        this.user = user;
        this.providersByName = ImmutableMap.copyOf(providersByName);
        this.credentialsId = credentialsId;

        // TODO: Remove Lists.newArrayList() when removing method updateInBatchTransactions(), see bellow.
        //       This is necessary to get a list object we can use `add` and `remove` on.
        this.inBatchTransactions = Lists.newArrayList(transactions);

        if (data != null) {
            this.userData = new TransactionProcessorUserData();
            this.userData.setAccounts(data.getAccounts());
            this.userData.setCredentials(data.getCredentials());
            this.userData.setInStoreTransactions(data.getTransactions());
            this.userData.setLoanDataByAccount(data.getLoanDataByAccount());
        }

        transactionsToUpdateList = Sets.newHashSet();
        transactionsToDelete = Lists.newArrayList();
        categoryChangeRecords = Lists.newArrayList();
    }

    public void addCategoryChangeRecord(CategoryChangeRecord record) {
        categoryChangeRecords.add(record);
    }

    public void addTransactionToDelete(Transaction transaction) {
        transactionsToDelete.add(transaction);
    }

    public void addTransactionToUpdateListPresentInDb(String id) {
        // Only add transactions to the update list that are already present in the database
        // New transactions (not present in the database) can't really be updated
        // This list doesn't have any real purpose at the moment and we should strive to remove remove it
        // in order to make the state less complex
        if (getUserData().getInStoreTransactions().containsKey(id)){
            transactionsToUpdateList.add(id);
        }
    }

    List<CategoryChangeRecord> getCategoryChangeRecords() {
        return categoryChangeRecords;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public List<Transaction> getInBatchTransactions() {
        return inBatchTransactions;
    }

    // TODO: When we go over from TransactionProcessor V1 to V2, remove this method and instead use
    //       context.getTransactionsToSave().values() in TransferDetectionCommand.
    // TODO: Also remove Lists.newArrayLists() around `transactions` in constructor.
    public void updateInBatchTransactions(List<Transaction> newInBatchTransactions) {
        List<Transaction> oldInBatchTransactions = Lists.newArrayList(this.inBatchTransactions);
        this.inBatchTransactions.removeAll(oldInBatchTransactions);
        this.inBatchTransactions.addAll(newInBatchTransactions);
    }

    public Transaction getTransaction(final String id) {
        Optional<Transaction> t = inBatchTransactions.stream().filter(input -> input.getId().equals(id)).findFirst();
        return t.orElse(null);
    }

    public List<Transaction> getTransactionsToDelete() {
        return transactionsToDelete;
    }

    public HashMap<String, Transaction> getTransactionsToSave() {
        return transactionsToSave;
    }

    public Set<String> getTransactionsToUpdateList() {
        return transactionsToUpdateList;
    }

    /**
     * Please inject this into the command instead. Will not be modified as the commands are executing.
     */
    @Deprecated
    public User getUser() {
        return user;
    }

    public TransactionProcessorUserData getUserData() {
        return userData;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setUserData(TransactionProcessorUserData userData) {
        this.userData = userData;
    }

    public Provider getProvider() {
        Optional<Credentials> credentials = userData.getCredentials().stream()
                .filter(credentials1 -> Objects.equals(credentials1.getId(), credentialsId)).findFirst();

        Preconditions.checkState(credentials.isPresent(),
                String.format("Couldn't find credentials. Needle: %s Haystack: %s", credentialsId,
                        userData.getCredentials()));

        return providersByName.get(credentials.get().getProviderName());
    }

}
