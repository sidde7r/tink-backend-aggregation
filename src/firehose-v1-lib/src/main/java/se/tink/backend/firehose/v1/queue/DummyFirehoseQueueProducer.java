package se.tink.backend.firehose.v1.queue;

import java.util.List;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.follow.FollowItem;
import se.tink.libraries.date.Period;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;

public class DummyFirehoseQueueProducer implements FirehoseQueueProducer {

    @Override
    public void sendAccountMessage(String userId, FirehoseMessage.Type messageType, Account account) {
        // Deliberately left empty.
    }

    @Override
    public void sendAccountsMessage(String userId, FirehoseMessage.Type messageType, List<Account> accounts) {
        // Deliberately left empty.
    }

    @Override
    public void sendCredentialMessage(String userId, FirehoseMessage.Type messageType, Credentials credential) {
        // Deliberately left empty.
    }

    @Override
    public void sendFollowItem(String userId, FirehoseMessage.Type messageType, FollowItem followItem) {
        // Deliberately left empty.
    }

    @Override
    public void sendFollowItems(String userId, FirehoseMessage.Type messageType, List<FollowItem> followItems) {
        // Deliberately left empty.
    }

    @Override
    public void sendPeriodsMessage(String userId, FirehoseMessage.Type messageType, List<Period> periods) {
        // Deliberately left empty.
    }

    @Override
    public void sendSignableOperationMessage(String userId, FirehoseMessage.Type messageType,
            SignableOperation signableOperation) {
        // Deliberately left empty.
    }

    @Override
    public void sendStatisticsMessage(String userId, FirehoseMessage.Type messageType, List<Statistic> statistics) {
        // Deliberately left empty.
    }

    @Override
    public void sendTransactionMessage(String userId, FirehoseMessage.Type messageType, Transaction transaction) {
        // Deliberately left empty.
    }

    @Override
    public void sendTransactionsMessage(String userId, FirehoseMessage.Type messageType,
            List<Transaction> transactions) {
        // Deliberately left empty.
    }

    @Override
    public void sendUserConfigurationMessage(String userId, FirehoseMessage.Type messageType, List<String> userFlags,
            UserProfile userProfile) {
        // Deliberately left empty.
    }

    @Override
    public void sendActivitiesMessage(String userId, FirehoseMessage.Type messageType, List<Activity> activities) {
        // Deliberately left empty.
    }
}
