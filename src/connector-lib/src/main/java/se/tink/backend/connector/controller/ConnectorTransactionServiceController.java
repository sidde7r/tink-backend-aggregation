package se.tink.backend.connector.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.CreateTransactionAccountContainer;
import se.tink.backend.connector.rpc.CreateTransactionAccountEntity;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.connector.rpc.DeleteTransactionAccountEntity;
import se.tink.backend.connector.rpc.DeleteTransactionAccountsContainer;
import se.tink.backend.connector.rpc.DeleteTransactionEntity;
import se.tink.backend.connector.rpc.PartnerTransactionPayload;
import se.tink.backend.connector.rpc.TransactionAccountEntity;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.rpc.UpdateTransactionAccountContainer;
import se.tink.backend.connector.rpc.UpdateTransactionAccountEntity;
import se.tink.backend.connector.util.handler.AccountHandler;
import se.tink.backend.connector.util.handler.CRUDType;
import se.tink.backend.connector.util.handler.CredentialsHandler;
import se.tink.backend.connector.util.handler.TransactionHandler;
import se.tink.backend.connector.util.handler.UserHandler;
import se.tink.backend.connector.util.helper.LogHelper;
import se.tink.backend.connector.util.helper.MetricHelper;
import se.tink.backend.connector.util.helper.RepositoryHelper;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionExternalId;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.rpc.TransactionToDelete;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ConnectorTransactionServiceController {

    private static final LogUtils log = new LogUtils(ConnectorTransactionServiceController.class);

    private UserHandler userHandler;
    private CredentialsHandler credentialsHandler;
    private AccountHandler accountHandler;
    private TransactionHandler transactionHandler;
    private MetricHelper metricHelper;
    private RepositoryHelper repositoryHelper;
    private TransactionExternalIdRepository transactionExternalIdRepository;

    @Inject
    public ConnectorTransactionServiceController(UserHandler userHandler, CredentialsHandler credentialsHandler,
            AccountHandler accountHandler, TransactionHandler transactionHandler, MetricHelper metricHelper,
            RepositoryHelper repositoryHelper, TransactionExternalIdRepository transactionExternalIdRepository) {

        this.userHandler = userHandler;
        this.credentialsHandler = credentialsHandler;
        this.accountHandler = accountHandler;
        this.transactionHandler = transactionHandler;
        this.metricHelper = metricHelper;
        this.repositoryHelper = repositoryHelper;
        this.transactionExternalIdRepository = transactionExternalIdRepository;
    }

    public void ingestTransactions(String externalUserId, CreateTransactionAccountContainer container)
            throws RequestException {

        User user = userHandler.findUser(externalUserId);
        Credentials credentials = credentialsHandler.findCredentials(user, externalUserId);
        List<Account> accounts = accountHandler.findAccounts(user, credentials);

        ImmutableMap<String, Account> accountsByExternalId = Maps.uniqueIndex(accounts, Account::getBankId);
        List<TransactionExternalId> transactionExternalIdList = Lists.newArrayList();
        Map<String, List<Transaction>> transactionsByExternalAccountId = Maps.newHashMap();

        for (CreateTransactionAccountEntity transactionAccount : container.getTransactionAccounts()) {
            String externalAccountId = transactionAccount.getExternalId();
            Account account = accountsByExternalId.get(externalAccountId);

            if (account == null) {
                throw RequestError.ACCOUNT_NOT_FOUND.exception().withExternalAccountId(externalAccountId);
            }

            List<Transaction> transactionsForAccount = Lists.newArrayList();
            List<TransactionExternalId> transactionExternalIds = Lists.newArrayList();
            Set<String> replacedPendingIds = Sets.newHashSet();

            for (CreateTransactionEntity transactionEntity : transactionAccount.getTransactions()) {
                Transaction transaction = transactionHandler.mapToTinkModel(user, credentials, account,
                        transactionEntity);

                if (!Objects.equals(container.getType(), TransactionContainerType.HISTORICAL)) {
                    log.debug(credentials,
                            "Updating transaction: " + LogHelper.get(transaction, transactionEntity,
                                    externalAccountId));
                }

                String externalId = transactionEntity.getExternalId();

                transactionsForAccount.add(transaction);
                replacedPendingIds.addAll(getPendingIds(transaction));

                TransactionExternalId transactionExternalId = new TransactionExternalId();
                transactionExternalId.setTransactionId(transaction.getId());
                transactionExternalId.setExternalTransactionId(externalId);
                transactionExternalId.setUserId(user.getId());
                transactionExternalId.setAccountId(account.getId());
                transactionExternalIds.add(transactionExternalId);
            }

            log.info(credentials, String.format("Received %d transactions for account: %s",
                    transactionsForAccount.size(), LogHelper.get(account)));

            transactionsByExternalAccountId.put(externalAccountId, transactionsForAccount);

            for (Transaction transaction : transactionsForAccount) {
                String externalId = transaction.getInternalPayload(TransactionPayloadTypes.EXTERNAL_ID);
                if (replacedPendingIds.contains(externalId)) {
                    // Skip pending transactions for which there is a booked transaction available
                    log.info(user.getId(), "[transactionId: " + transaction.getId() + "]: Skipping transaction (non-pending in same batch).");
                    transactionsByExternalAccountId.get(externalAccountId).remove(transaction);
                    Optional<TransactionExternalId> transactionExternalId = transactionExternalIds.stream()
                            .filter(t -> Objects.equals(t.getExternalTransactionId(), externalId))
                            .findFirst();
                    if (transactionExternalId.isPresent()) {
                        transactionExternalIds.remove(transactionExternalId.get());
                    } else {
                        log.error(user.getId(), "TransactionExternalId should be there...");
                    }
                }
            }

            canIngestTransactions(transactionExternalIds, user, account, externalUserId, externalAccountId);
            transactionExternalIdList.addAll(transactionExternalIds);
            repositoryHelper.saveTransactionExternalIds(transactionExternalIds);
        }

        List<Transaction> allTransactions = transactionsByExternalAccountId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        metricHelper.reportTransactionMetric(allTransactions, container.getType());

        if (!allTransactions.isEmpty()) {
            try {
                transactionHandler.ingestTransactions(user, credentials, allTransactions, container.getType());
            } catch (ExecutionException e) {
                transactionExternalIdList.forEach(transactionExternalIdRepository::delete);
                throw new RuntimeException(e);
            }
        }

        for (TransactionAccountEntity transactionAccount : container.getTransactionAccounts()) {
            accountHandler.updateAccount(container.getType(),
                    accountsByExternalId.get(transactionAccount.getExternalId()),
                    transactionsByExternalAccountId.get(transactionAccount.getExternalId()), transactionAccount,
                    CRUDType.CREATE);
        }
    }

    public void deleteTransactions(String externalUserId, DeleteTransactionAccountsContainer container)
            throws RequestException {

        User user = userHandler.findUser(externalUserId);
        ImmutableMap<String, Account> accountsByExternalId = getAccountsByExternalId(user, externalUserId);
        List<TransactionToDelete> transactionsToDelete = Lists.newArrayList();
        Map<String, List<Transaction>> transactionsByExternalAccountId = Maps.newHashMap();

        for (DeleteTransactionAccountEntity transactionAccount : container.getTransactionAccounts()) {
            final String externalAccountId = transactionAccount.getExternalId();
            final Account account = accountsByExternalId.get(externalAccountId);
            List<Transaction> transactionsInStore = Lists.newArrayList();

            if (account == null) {
                throw RequestError.ACCOUNT_NOT_FOUND.exception().withExternalAccountId(externalAccountId);
            }

            for (DeleteTransactionEntity transactionEntity : transactionAccount.getTransactions()) {

                Optional<Transaction> transactionInStore = repositoryHelper
                        .getTransactionFromId(account.getId(), account.getUserId(),
                                transactionEntity.getExternalId(), true);

                if (!transactionInStore.isPresent()) {
                    throw RequestError.TRANSACTION_NOT_FOUND.exception().withExternalAccountId(externalAccountId)
                            .withExternalTransactionId(transactionEntity.getExternalId());
                }

                TransactionToDelete transactionToDelete = TransactionToDelete
                        .create(transactionEntity.getExternalId(), account.getId());
                transactionsToDelete.add(transactionToDelete);
                transactionsInStore.add(transactionInStore.get());
            }

            transactionsByExternalAccountId.put(externalAccountId, transactionsInStore);
        }

        transactionHandler.deleteTransactions(transactionsToDelete, user);

        for (TransactionAccountEntity transactionAccount : container.getTransactionAccounts()) {
            accountHandler.updateAccount(container.getType(),
                    accountsByExternalId.get(transactionAccount.getExternalId()),
                    transactionsByExternalAccountId.get(transactionAccount.getExternalId()), transactionAccount,
                    CRUDType.DELETE);
        }
    }

    public void updateTransactions(String externalUserId, String transactionId,
            UpdateTransactionAccountContainer container) throws RequestException {

        // UpdateTransactionAccountContainer guarantees altering exactly one account with one transaction per API request
        UpdateTransactionAccountEntity accountEntity = container.getTransactionAccounts().get(0);
        CreateTransactionEntity transactionEntity = accountEntity.getTransactions().get(0);

        boolean usesExternalId = transactionEntity.getExternalId() != null;

        if (!Objects.equals(transactionId, transactionEntity.getExternalId()) &&
                !Objects.equals(transactionId, transactionEntity.getTinkId())) {
            throw RequestError.TRANSACTION_ID_NOT_MATCHING.exception().withExternalTransactionId(transactionId);
        }

        User user = userHandler.findUser(externalUserId);

        final Account account;
        if (accountEntity.getExternalId() == null) {
            account = repositoryHelper.getAccount(user, accountEntity.getTinkId());
        } else {
            ImmutableMap<String, Account> accountsByExternalId = getAccountsByExternalId(user, externalUserId);
            account = accountsByExternalId.get(accountEntity.getExternalId());
        }

        if (account == null) {
            throw RequestError.ACCOUNT_NOT_FOUND.exception().withExternalAccountId(accountEntity.getExternalId());
        }

        Optional<Transaction> transactionInStore = repositoryHelper.getTransactionFromId(account.getId(), user.getId(),
                transactionId, usesExternalId);

        if (!transactionInStore.isPresent()) {
            throw RequestError.TRANSACTION_NOT_FOUND.exception().withExternalTransactionId(transactionId);
        }

        Transaction newTransaction = transactionHandler
                .mapUpdateToTinkModel(transactionInStore.get().clone(), transactionEntity);

        transactionHandler.updateTransaction(newTransaction);

        accountHandler.updateAccount(container.getType(), account,
                Lists.newArrayList(newTransaction, transactionInStore.get()), accountEntity, CRUDType.UPDATE);
    }

    private ImmutableMap<String, Account> getAccountsByExternalId(User user, String externalUserId)
            throws RequestException {
        Credentials credentials = credentialsHandler.findCredentials(user, externalUserId);
        List<Account> accounts = accountHandler.findAccounts(user, credentials);
        return Maps.uniqueIndex(accounts, Account::getBankId);
    }

    private List<String> getPendingIds(Transaction transaction) {
        String partnerPayload = transaction.getInternalPayload(Transaction.InternalPayloadKeys.PARTNER_PAYLOAD);
        if (partnerPayload != null) {
            PartnerTransactionPayload partnerTransactionPayload = SerializationUtils
                    .deserializeFromString(partnerPayload, PartnerTransactionPayload.class);
            return partnerTransactionPayload.getPendingIds();
        }
        return Lists.newArrayList();
    }

    private void canIngestTransactions(List<TransactionExternalId> externalTransactionIds, User user, Account account,
            String externalUserId, String externalAccountId) throws RequestException {

        List<String> transactionIds = externalTransactionIds.stream()
                .map(TransactionExternalId::getExternalTransactionId).collect(Collectors.toList());
        List<TransactionExternalId> storedTransactions = repositoryHelper
                .getListOfExternalTransactionIds(account.getId(), user.getId(), transactionIds);

        List<String> alreadyDeleted = storedTransactions.stream()
                .filter(TransactionExternalId::isDeleted)
                .map(TransactionExternalId::getExternalTransactionId).collect(Collectors.toList());

        if (alreadyDeleted.size() != 0) {
            throw RequestError.TRANSACTION_ALREADY_DELETED.exception()
                    .withExternalTransactionId(String.join(",", alreadyDeleted))
                    .withExternalAccountId(externalAccountId)
                    .withExternalUserId(externalUserId);
        }

        List<String> alreadyExists = storedTransactions.stream()
                .map(TransactionExternalId::getExternalTransactionId).collect(Collectors.toList());

        if (alreadyExists.size() != 0) {
            throw RequestError.TRANSACTION_CONFLICT.exception()
                    .withExternalTransactionId(String.join(",", alreadyExists))
                    .withExternalAccountId(externalAccountId)
                    .withExternalUserId(externalUserId);
        }
    }
}
