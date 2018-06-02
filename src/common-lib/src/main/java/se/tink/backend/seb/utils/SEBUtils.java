package se.tink.backend.seb.utils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.assertj.core.util.Lists;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.connector.rpc.seb.AccountEntity;
import se.tink.backend.connector.rpc.seb.PartnerTransactionPayload;
import se.tink.backend.connector.rpc.seb.TransactionEntity;
import se.tink.backend.connector.rpc.seb.TransactionStatus;
import se.tink.backend.connector.rpc.seb.UserEntity;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Market;
import se.tink.backend.core.NotificationSettings;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SEBUtils {

    public static final String SEB_PROVIDER_NAME = "se-seb-abstract";

    public static Account createAccount(AccountEntity accountEntity) {
        Account account = new Account();

        account.setBankId(accountEntity.getExternalId());
        account.setAccountNumber(accountEntity.getNumber());
        account.setAvailableCredit(accountEntity.getAvailableCredit() != null ? accountEntity.getAvailableCredit() : 0);

        // If there is a disposable amount, use it.

        if (accountEntity.getDisposableAmount() != null) {
            account.setBalance(accountEntity.getDisposableAmount());
        } else {
            account.setBalance(accountEntity.getBalance());
        }

        account.setName(accountEntity.getName());
        account.setType(accountEntity.getType());

        return account;
    }

    public static Account createAccount(AccountEntity accountEntity, User user, Credentials credentials) {
        Account account = createAccount(accountEntity);

        account.setUserId(user.getId());
        account.setCredentialsId(credentials.getId());

        return account;
    }

    public static Credentials createCredentials(User user) {
        Credentials credentials = new Credentials();

        credentials.setUserId(user.getId());
        credentials.setProviderName(SEBUtils.SEB_PROVIDER_NAME);
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setType(CredentialsTypes.PASSWORD);

        return credentials;
    }

    public static User createUser(UserEntity userEntity, Market market, FlagsConfiguration flagsConfiguration) {
        User user = new User();

        UserProfile userProfile = UserProfile.createDefault(market);

        // Explicitly setting the settings according to SEB requirements.

        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setEinvoices(true);
        notificationSettings.setDoubleCharge(true);
        notificationSettings.setBudget(false);
        notificationSettings.setFraud(false);
        notificationSettings.setBalance(true);
        notificationSettings.setIncome(false);
        notificationSettings.setLargeExpense(true);
        notificationSettings.setSummaryMonthly(false);
        notificationSettings.setTransaction(false);
        notificationSettings.setUnusualAccount(false);
        notificationSettings.setSummaryWeekly(false);
        notificationSettings.setUnusualCategory(false);

        userProfile.setNotificationSettings(notificationSettings);

        user.setProfile(userProfile);
        user.setUsername(userEntity.getExternalId());
        user.setCreated(new Date());
        user.setFlags(getFeatureFlags(flagsConfiguration, market));

        return user;
    }

    public static Transaction createTransaction(User user, Credentials credentials, Account account,
            TransactionEntity transactionEntity, boolean enforceRemovalOfPendingAfterExpired) {
        boolean isPending = Objects.equals(transactionEntity.getStatus(), TransactionStatus.RESERVED);

        Transaction transaction = new Transaction();

        transaction.setAccountId(account.getId());
        transaction.setCredentialsId(credentials.getId());
        transaction.setUserId(user.getId());
        transaction.setAmount(transactionEntity.getAmount());
        transaction.setDate(transactionEntity.getDate());
        transaction.setDescription(transactionEntity.getDescription());
        transaction.setType(transactionEntity.getType());
        transaction.setPending(isPending);

        transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, transactionEntity.getExternalId());
        transaction.setInternalPayload(Transaction.InternalPayloadKeys.INCOMING_TIMESTAMP,
                String.valueOf(transactionEntity.getEntityCreated().getTime()));

        Object expirationDate = transactionEntity.getPayload()
                .get(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE);

        // If the config says to always enforce removal of pending transactions, and we didn't get an expiration date in
        // the payload, then we set an expiration date manually here so that it can be removed correctly.
        if (isPending && enforceRemovalOfPendingAfterExpired && expirationDate == null) {
            transactionEntity.getPayload().put(PartnerTransactionPayload.PENDING_TRANSACTION_EXPIRATION_DATE,
                    DateUtils.addDays(new Date(), 4).getTime());
        }

        if (transactionEntity.getPayload() != null) {
            String serializedPayload = SerializationUtils.serializeToString(transactionEntity.getPayload());
            transaction.setInternalPayload(Transaction.InternalPayloadKeys.SEB_PAYLOAD, serializedPayload);
        }

        return transaction;
    }

    public static String getDepotNumberForHolding(String depotNumber) {
        return depotNumber.replaceAll("^01[0]*", "");
    }
    
    private static List<String> getFeatureFlags(FlagsConfiguration flagsConfiguration, Market market) {
        if (flagsConfiguration == null) {
            return Collections.emptyList();
        }
        Map<String, Map<String, Double>> flagsByMarket = flagsConfiguration.getRegister();

        if (flagsByMarket == null || flagsByMarket.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> flagsToAdd = Lists.newArrayList();

        // Add market specific flags.
        if (market != null) {
            Map<String, Double> marketFlags = flagsByMarket.get(market.getCodeAsString());
            if (marketFlags != null) {
                flagsToAdd.addAll(marketFlags.keySet());
            }
        }

        // Add general flags.
        Map<String, Double> generalFlags = flagsByMarket.get("ALL");
        if (generalFlags != null) {
            flagsToAdd.addAll(generalFlags.keySet());
        }

        return flagsToAdd;
    }
}
