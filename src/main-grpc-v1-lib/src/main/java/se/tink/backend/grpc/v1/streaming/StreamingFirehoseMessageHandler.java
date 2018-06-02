package se.tink.backend.grpc.v1.streaming;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.backend.firehose.v1.models.FollowItem;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.streaming.StreamingConverters;
import se.tink.backend.grpc.v1.converter.user.CoreUserToUserConfigurationConverter;
import se.tink.backend.grpc.v1.streaming.flags.DynamicFeatureFlagsUpdater;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.backend.main.controllers.AccountServiceController;
import se.tink.backend.main.controllers.FollowServiceController;
import se.tink.backend.main.controllers.UserServiceController;
import se.tink.grpc.v1.models.Accounts;
import se.tink.grpc.v1.models.Credentials;
import se.tink.grpc.v1.models.FollowItems;
import se.tink.grpc.v1.models.Periods;
import se.tink.grpc.v1.models.SignableOperations;
import se.tink.grpc.v1.models.Transactions;
import se.tink.grpc.v1.models.UserConfiguration;
import se.tink.grpc.v1.rpc.StreamingResponse;

public class StreamingFirehoseMessageHandler {
    private final FollowServiceController followServiceController;
    private final UserServiceController userServiceController;
    private final UserRepository userRepository;
    private final AccountServiceController accountServiceController;
    private final DynamicFeatureFlagsUpdater dynamicFeatureFlagsUpdater;

    @Inject
    public StreamingFirehoseMessageHandler(
            FollowServiceController followServiceController,
            UserServiceController userServiceController,
            UserRepository userRepository,
            AccountServiceController accountServiceController,
            DynamicFeatureFlagsUpdater dynamicFeatureFlagsUpdater) {
        this.followServiceController = followServiceController;
        this.userServiceController = userServiceController;
        this.userRepository = userRepository;
        this.accountServiceController = accountServiceController;
        this.dynamicFeatureFlagsUpdater = dynamicFeatureFlagsUpdater;
    }

    public List<StreamingResponse> handle(FirehoseMessage message, StreamingConverters converters) {
        User user = userRepository.findOne(message.getUserId());
        List<StreamingResponse> result = Lists.newArrayList();
        StreamingResponse.Type type = EnumMappers.FIREHOSE_MESSAGE_TYPE_TO_GRPC_STREAMING_TYPE_MAP
                .getOrDefault(message.getType(), StreamingResponse.Type.READ);

        Periods periodsMap = converters.convertPeriods(message.getPeriodsList());
        Transactions transactions = converters.convertTransactions(message.getTransactionsList());

        StreamingResponse.Builder streamingResponseBuilder = StreamingResponse
                .newBuilder()
                .setType(type);

        streamingResponseBuilder.setTimestamp(ProtobufModelUtils.getCurrentProtobufTimestamp());

        if (message.getSignableOperationsList() != null && !message.getSignableOperationsList().isEmpty()) {
            SignableOperations signableOperations = converters
                    .convertSignableOperations(message.getSignableOperationsList());
            streamingResponseBuilder.setSignableOperations(signableOperations);
        }

        if (message.getTransactionsList() != null && !message.getTransactionsList().isEmpty()) {
            streamingResponseBuilder.setTransactions(transactions);
        }

        if (message.getPeriodsList() != null && !message.getPeriodsList().isEmpty()) {
            streamingResponseBuilder.setPeriods(periodsMap);
        }

        if (message.getAccountsList() != null && !message.getAccountsList().isEmpty()) {
            Accounts accounts = converters.convertAccounts(message.getAccountsList());
            streamingResponseBuilder.setAccounts(accounts);

            //Update residence tab whenever an account is updated.
            List<Account> allAccounts = accountServiceController.list(user.getId());
            dynamicFeatureFlagsUpdater.updateResidenceTabFlag(user, allAccounts);

            //Covert to user configuration so that flag will be updated on app.
            UserConfiguration userConfiguration = new CoreUserToUserConfigurationConverter().convertFrom(user);
            streamingResponseBuilder.setUserConfiguration(userConfiguration);
        }

        if (message.getCredentialsList() != null && !message.getCredentialsList().isEmpty()) {
            Credentials credentials = converters.convertCredentials(message.getCredentialsList(), user.getLocale());
            streamingResponseBuilder.setCredentials(credentials);
        }

        if (message.getStatisticsList() != null && !message.getStatisticsList().isEmpty()) {
            streamingResponseBuilder
                    .setStatistics(converters.convertStatistics(message.getStatisticsList(), user.getProfile()));
        }

        if (message.hasUserConfiguration()) {
            streamingResponseBuilder
                    .setUserConfiguration(converters.convertUserConfiguration(message.getUserConfiguration()));
        }

        if (message.getFollowItemsCount() > 0) {
            streamingResponseBuilder.setFollowItems(convertFirehoseFollowItems(message, user, converters));
        }

        result.add(streamingResponseBuilder.build());

        if (!transactions.getTransactionList().isEmpty() || !periodsMap.getPeriodMap().isEmpty()) {
            // With changing transactions and periods, FollowData could be change as well. Because of follow data are
            // not in DB, resend all follow items to be consistent.
            result.add(createFollowItemsResponse(user, converters));
        }
        return result;
    }

    private FollowItems convertFirehoseFollowItems(FirehoseMessage message, User user,
            StreamingConverters converters) {
        if (message.getFollowItemsCount() > 0) {
            if (Objects.equals(message.getType(), FirehoseMessage.Type.DELETE)) {
                return converters.convertFollowItems(message.getFollowItemsList());
            }
            // Fetch FollowItems with data and convert them
            return converters.convertFollowItems(message.getFollowItemsList().stream()
                            .map(FollowItem::getId)
                            .map(id -> followServiceController.get(user, id, null))
                            .collect(Collectors.toList()),
                    userServiceController.getPeriods(user));
        }
        return FollowItems.getDefaultInstance();
    }

    private StreamingResponse createFollowItemsResponse(User user, StreamingConverters converters) {
        return StreamingResponse
                .newBuilder()
                .setType(StreamingResponse.Type.UPDATE)
                .setTimestamp(ProtobufModelUtils.getCurrentProtobufTimestamp())
                .setFollowItems(converters.convertFollowItems(followServiceController.list(user, true, null),
                        userServiceController.getPeriods(user)))
                .build();
    }
}
