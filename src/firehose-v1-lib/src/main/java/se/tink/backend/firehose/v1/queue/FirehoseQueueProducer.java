package se.tink.backend.firehose.v1.queue;

import java.util.List;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.libraries.date.Period;

public interface FirehoseQueueProducer {
    void sendAccountMessage(String userId, FirehoseMessage.Type messageType,
            se.tink.backend.core.Account account);

    void sendAccountsMessage(String userId, FirehoseMessage.Type messageType, List<Account> accounts);

    void sendCredentialMessage(String userId, FirehoseMessage.Type messageType,
            se.tink.backend.core.Credentials credential);

    void sendFollowItem(String userId, FirehoseMessage.Type messageType, FollowItem followItem);

    void sendFollowItems(String userId, FirehoseMessage.Type messageType, List<FollowItem> followItems);

    void sendPeriodsMessage(String userId, FirehoseMessage.Type messageType, List<Period> periods);

    void sendSignableOperationMessage(String userId, FirehoseMessage.Type messageType,
            SignableOperation signableOperation);

    void sendStatisticsMessage(String userId, FirehoseMessage.Type messageType, List<Statistic> statistics);

    void sendTransactionMessage(String userId, FirehoseMessage.Type messageType, Transaction transaction);

    void sendTransactionsMessage(String userId, FirehoseMessage.Type messageType, List<Transaction> transactions);

    void sendUserConfigurationMessage(String userId, FirehoseMessage.Type messageType, List<String> userFlags, UserProfile userProfile);

    void sendActivitiesMessage(String userId, FirehoseMessage.Type messageType, List<Activity> activities);
}
