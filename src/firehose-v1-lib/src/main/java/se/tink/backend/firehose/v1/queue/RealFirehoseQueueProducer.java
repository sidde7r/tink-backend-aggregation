package se.tink.backend.firehose.v1.queue;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Activity;
import se.tink.backend.core.follow.FollowItem;
import se.tink.libraries.date.Period;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserProfile;
import se.tink.backend.firehose.v1.models.Account;
import se.tink.backend.firehose.v1.models.Credential;
import se.tink.backend.firehose.v1.models.SignableOperation;
import se.tink.backend.firehose.v1.models.Statistic;
import se.tink.backend.firehose.v1.models.UserConfiguration;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.queue.QueueProducer;

public class RealFirehoseQueueProducer implements FirehoseQueueProducer {
    private static final LogUtils log = new LogUtils(RealFirehoseQueueProducer.class);

    private final QueueProducer queueProducer;
    private final ModelMapper modelMapper;

    public RealFirehoseQueueProducer(QueueProducer queueProducer) {
        this.queueProducer = Preconditions.checkNotNull(queueProducer);

        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(mappingContext -> mappingContext.getSource() != null);

        FirehoseModelConverters.addConverters(modelMapper);
    }

    public void sendAccountMessage(String userId, FirehoseMessage.Type messageType,
            se.tink.backend.core.Account account) {
        sendAccountsMessage(userId, messageType, Collections.singletonList(account));
    }

    public void sendAccountsMessage(String userId, FirehoseMessage.Type messageType,
            List<se.tink.backend.core.Account> accounts) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder().setType(messageType);

            for (se.tink.backend.core.Account account : accounts) {
                messageBuilder.addAccounts(modelMapper.map(account, Account.class));
            }

            send(FirehoseTopics.ACCOUNTS, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, accounts.toString(), "Account(s) could not be written to Firehose.", e);
        }
    }

    public void sendCredentialMessage(String userId, FirehoseMessage.Type messageType,
            se.tink.backend.core.Credentials credential) {
        sendCredentialsMessage(userId, messageType, Collections.singletonList(credential));
    }

    @Override
    public void sendFollowItem(String userId, FirehoseMessage.Type messageType, FollowItem followItem) {
        sendFollowItems(userId, messageType, Collections.singletonList(followItem));
    }

    @Override
    public void sendFollowItems(String userId, FirehoseMessage.Type messageType, List<FollowItem> followItems) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder().setType(messageType);

            followItems.stream()
                    .map(followItem -> modelMapper
                            .map(followItem, se.tink.backend.firehose.v1.models.FollowItem.Builder.class))
                    .forEach(messageBuilder::addFollowItems);

            send(FirehoseTopics.FOLLOW_ITEMS, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, "FollowItems could not be written to Firehose.", e);
        }
    }

    private void sendCredentialsMessage(String userId, FirehoseMessage.Type messageType,
            List<se.tink.backend.core.Credentials> credentials) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder().setType(messageType);

            for (se.tink.backend.core.Credentials credential : credentials) {
                messageBuilder.addCredentials(modelMapper.map(credential, Credential.class));
            }

            send(FirehoseTopics.CREDENTIALS, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, credentials.toString(), "Credentials could not be written to Firehose.", e);
        }
    }

    @Override
    public void sendPeriodsMessage(String userId, FirehoseMessage.Type messageType, List<Period> periods) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder()
                    .setType(messageType)
                    .addAllPeriods(periods.stream()
                            .map(period -> modelMapper
                                    .map(period, se.tink.backend.firehose.v1.models.Period.Builder.class)
                                    .build())
                            .collect(Collectors.toList()));

            send(FirehoseTopics.PERIODS, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, periods.toString(), "Periods could not be written to Firehose.", e);
        }
    }

    @Override
    public void sendSignableOperationMessage(String userId, FirehoseMessage.Type messageType,
            se.tink.backend.core.signableoperation.SignableOperation signableOperation) {
        sendSignableOperationsMessage(userId, messageType, Collections.singletonList(signableOperation));
    }

    private void sendSignableOperationsMessage(String userId, FirehoseMessage.Type messageType,
            List<se.tink.backend.core.signableoperation.SignableOperation> signableOperations) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder().setType(messageType);

            signableOperations.stream()
                    .map(signableOperation -> modelMapper.map(signableOperation, SignableOperation.Builder.class))
                    .forEach(messageBuilder::addSignableOperations);

            send(FirehoseTopics.SIGNABLE_OPERATIONS, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, "Credentials could not be written to Firehose.", e);
        }
    }

    public void sendStatisticsMessage(String userId, FirehoseMessage.Type messageType,
            List<se.tink.backend.core.Statistic> statistics) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder().setType(messageType);

            for (se.tink.backend.core.Statistic statistic : statistics) {
                messageBuilder.addStatistics(modelMapper.map(statistic, Statistic.Builder.class));
            }

            send(FirehoseTopics.STATISTICS, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, statistics.toString(), "Statistics could not be written to Firehose.", e);
        }
    }

    @Override
    public void sendTransactionMessage(String userId, FirehoseMessage.Type messageType, Transaction transaction) {
        sendTransactionsMessage(userId, messageType, Collections.singletonList(transaction));
    }

    @Override
    public void sendTransactionsMessage(String userId, FirehoseMessage.Type messageType,
            List<Transaction> transactions) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder()
                    .setType(messageType)
                    .addAllTransactions(transactions.stream()
                            .map(transaction -> modelMapper
                                    .map(transaction, se.tink.backend.firehose.v1.models.Transaction.Builder.class)
                                    .build())
                            .collect(Collectors.toList()));

            send(FirehoseTopics.TRANSACTIONS, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, transactions.toString(), "Transactions could not be written to Firehose.", e);
        }
    }

    @Override
    public void sendUserConfigurationMessage(String userId, FirehoseMessage.Type messageType, List<String> userFlags,
            UserProfile userProfile) {
        try {
            UserConfiguration.Builder userConfigurationBuilder = UserConfiguration.newBuilder();
            Optional.ofNullable(userFlags).ifPresent(userConfigurationBuilder::addAllFlags);
            Optional.ofNullable(userProfile)
                    .map(profile -> modelMapper.map(profile, UserConfiguration.I18NConfiguration.class))
                    .ifPresent(userConfigurationBuilder::setI18NConfiguration);

            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder()
                    .setType(messageType)
                    .setUserConfiguration(userConfigurationBuilder);
            send(FirehoseTopics.USER_CONFIGURATION, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, "User configuration could not be written to Firehose.", e);
        }

    }

    @Override
    public void sendActivitiesMessage(String userId, FirehoseMessage.Type messageType, List<Activity> activities) {
        try {
            FirehoseMessage.Builder messageBuilder = FirehoseMessage.newBuilder()
                    .setType(messageType)
                    .addAllActivities(activities.stream()
                            .map(FirehoseModelConverters::fromCoreToFirehose)
                            .collect(Collectors.toList()));

            send(FirehoseTopics.ACTIVITIES, userId, messageBuilder);
        } catch (Exception e) {
            log.warn(userId, activities.toString(), "Activities could not be written to Firehose.", e);
        }
    }

    private void send(String topic, String userId, FirehoseMessage.Builder messageBuilder) {
        messageBuilder.setUserId(userId);
        queueProducer.send(topic, userId, messageBuilder.build());
    }
}
