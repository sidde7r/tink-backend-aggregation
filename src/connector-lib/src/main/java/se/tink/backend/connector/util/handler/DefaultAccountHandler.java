package se.tink.backend.connector.util.handler;

import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.AccountEntity;
import se.tink.backend.connector.rpc.PartnerAccountPayload;
import se.tink.backend.connector.rpc.TransactionAccountEntity;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.util.helper.LogHelper;
import se.tink.backend.connector.util.helper.RepositoryHelper;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.rpc.DeleteAccountRequest;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DefaultAccountHandler implements AccountHandler {

    private static final LogUtils log = new LogUtils(DefaultAccountHandler.class);

    private RepositoryHelper repositoryHelper;
    private SystemServiceFactory systemServiceFactory;
    private BalanceHandler balanceHandler;

    @Inject DefaultAccountHandler(RepositoryHelper repositoryHelper, SystemServiceFactory systemServiceFactory,
            BalanceHandler balanceHandler) {
        this.repositoryHelper = repositoryHelper;
        this.systemServiceFactory = systemServiceFactory;
        this.balanceHandler = balanceHandler;
    }

    @Override
    public List<Account> findAccounts(User user, Credentials credentials) throws RequestException {
        List<Account> accounts = repositoryHelper.getAccounts(user, credentials);

        if (accounts.isEmpty()) {
            throw RequestError.NO_ACCOUNTS_FOUND.exception();
        }

        return accounts;
    }

    @Override
    public Account mapToTinkModel(AccountEntity accountEntity, User user, Credentials credentials)
            throws RequestException {

        Account account = new Account();
        account.setBankId(accountEntity.getExternalId());
        account.setAccountNumber(accountEntity.getNumber());
        account.setAvailableCredit(accountEntity.getAvailableCredit() != null ? accountEntity.getAvailableCredit() : 0);
        account.setName(accountEntity.getName());
        account.setType(accountEntity.getType());
        account.setUserId(user.getId());
        account.setCredentialsId(credentials.getId());

        String serializedPayload = SerializationUtils.serializeToString(accountEntity.getPayload());
        account.putPayload(Account.PayloadKeys.PARTNER_PAYLOAD, serializedPayload);

        if (Objects.equals(accountEntity.getPayload().get(PartnerAccountPayload.CALCULATE_BALANCE), true)) {
            account.setBalance(0);
        } else {
            balanceHandler.setNewBalance(account, accountEntity.getReservedAmount(), accountEntity.getBalance(),
                    accountEntity.getPayload());
        }

        return account;
    }

    @Override
    public void updateAccount(TransactionContainerType type, Account account, List<Transaction> transactions,
            TransactionAccountEntity transactionAccount, CRUDType crudType) {

        // Here we don't care about HISTORICAL type since the balance depends on all transactions.
        if (shouldCalculateBalance(account, transactionAccount.getPayload())) {
            balanceHandler.calculateAndUpdateBalance(account, crudType, transactions);

        } else if (!Objects.equals(type, TransactionContainerType.HISTORICAL)) {

            // Don't update account on HISTORICAL types (initial load & re-syncs). In both these cases the partner
            // should do ingest accounts, with the latest info, prior to an ingest transactions with HISTORICAL
            // containerType.
            balanceHandler.setNewBalance(account, transactionAccount.getReservedAmount(),
                    transactionAccount.getBalance(), transactionAccount.getPayload());
            storeAccount(account);
        }
    }

    private boolean shouldCalculateBalance(Account account, Map<String, Object> transactionAccountPayload) {
        Optional<PartnerAccountPayload> payload = getAccountPayload(account);

        return payload.isPresent()
                && payload.get().isCalculateBalance()
                && transactionAccountPayload.containsKey(PartnerAccountPayload.CALCULATE_BALANCE);
    }

    private Optional<PartnerAccountPayload> getAccountPayload(Account account) {
        String serializedPayload = account.getPayload(Account.PayloadKeys.PARTNER_PAYLOAD);

        if (serializedPayload == null) {
            return Optional.empty();
        }

        PartnerAccountPayload payload = SerializationUtils
                .deserializeFromString(serializedPayload, PartnerAccountPayload.class);

        return Optional.ofNullable(payload);
    }

    @Override
    public void storeAccount(Account account) {
        UpdateAccountRequest updateAccountsRequest = new UpdateAccountRequest();

        updateAccountsRequest.setAccount(account);
        updateAccountsRequest.setAccountFeatures(AccountFeatures.createEmpty());
        updateAccountsRequest.setCredentialsId(account.getCredentialsId());
        updateAccountsRequest.setUser(account.getUserId());

        systemServiceFactory.getUpdateService().updateAccount(updateAccountsRequest);

        log.debug(account.getUserId(), "Updating account: " + LogHelper.get(account));
    }

    @Override
    public void deleteAccount(Account account, Credentials credentials, User user) {
        DeleteAccountRequest deleteAccountRequest = new DeleteAccountRequest();
        deleteAccountRequest.setUserId(user.getId());
        deleteAccountRequest.setCredentialsId(credentials.getId());
        deleteAccountRequest.setAccountId(account.getId());

        systemServiceFactory.getUpdateService().deleteAccount(deleteAccountRequest);

        log.info(credentials, "Deleting account: " + LogHelper.get(account));
    }
}
