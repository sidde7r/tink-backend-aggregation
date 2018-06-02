package se.tink.backend.grpc.v1.converter.streaming;

import com.google.common.collect.BiMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.core.UserProfile;
import se.tink.backend.firehose.v1.models.Statistic;
import se.tink.backend.grpc.v1.converter.account.FirehoseAccountToGrpcAccountConverter;
import se.tink.backend.grpc.v1.converter.credential.FirehoseCredentialToGrpcCredentialConverter;
import se.tink.backend.grpc.v1.converter.follow.CoreFollowItemToGrpcFollowItemConverter;
import se.tink.backend.grpc.v1.converter.follow.FirehoseFollowItemToGrpcFollowItemConverter;
import se.tink.backend.grpc.v1.converter.follow.FollowItemGrpcConverter;
import se.tink.backend.grpc.v1.converter.periods.CorePeriodToGrpcPeriodConverter;
import se.tink.backend.grpc.v1.converter.periods.FirehosePeriodToGrpcPeriodConverter;
import se.tink.backend.grpc.v1.converter.statistic.FirehoseStatisticsToGrpcStatisticTreeConverter;
import se.tink.backend.grpc.v1.converter.transaction.FirehoseTransactionToGrpcTransactionConverter;
import se.tink.backend.grpc.v1.converter.transfer.FirehoseSignableOperationToGrpcSignableOperationConverter;
import se.tink.backend.grpc.v1.converter.user.FirehoseUserConfigurationToGrpcUserConfigurationConverter;
import se.tink.backend.utils.ProviderImageMap;
import se.tink.grpc.v1.models.Accounts;
import se.tink.grpc.v1.models.Credentials;
import se.tink.grpc.v1.models.FollowItems;
import se.tink.grpc.v1.models.Period;
import se.tink.grpc.v1.models.Periods;
import se.tink.grpc.v1.models.SignableOperations;
import se.tink.grpc.v1.models.StatisticTree;
import se.tink.grpc.v1.models.Transactions;
import se.tink.grpc.v1.models.UserConfiguration;

public class StreamingConverters {
    private final BiMap<String, String> categoryCodeById;
    private Map<String, Provider> providersByCredentialIds;
    private final String userCurrencyCode;
    private final ProviderImageMap providerImageMap;

    private final CredentialsRepository credentialsRepository;
    private final ProviderDao providerDao;

    public StreamingConverters(BiMap<String, String> categoryCodeById,
            Map<String, Provider> providersByCredentialIds,
            String userCurrencyCode, ProviderImageMap providerImageMap,
            CredentialsRepository credentialsRepository, ProviderDao providerDao) {
        this.categoryCodeById = categoryCodeById;
        this.providersByCredentialIds = providersByCredentialIds;
        this.userCurrencyCode = userCurrencyCode;
        this.providerImageMap = providerImageMap;

        this.credentialsRepository = credentialsRepository;
        this.providerDao = providerDao;
    }

    public Accounts convertAccounts(List<se.tink.backend.firehose.v1.models.Account> accounts) {
        return new FirehoseAccountToGrpcAccountConverter(userCurrencyCode, providersByCredentialIds, providerImageMap,
                credentialsRepository, providerDao).convertFrom(accounts);
    }

    public Credentials convertCredentials(List<se.tink.backend.firehose.v1.models.Credential> credentials, String locale) {
        return new FirehoseCredentialToGrpcCredentialConverter(locale).convertFrom(credentials);
    }

    public FollowItems convertFollowItems(List<se.tink.backend.core.follow.FollowItem> followItems,
            List<se.tink.libraries.date.Period> userPeriods) {
        CorePeriodToGrpcPeriodConverter periodConverter = new CorePeriodToGrpcPeriodConverter();
        Map<String, Period> grpcPeriodMap = userPeriods.stream()
                .collect(Collectors.toMap(se.tink.libraries.date.Period::getName, periodConverter::convertFrom));
        FollowItemGrpcConverter converter = new FollowItemGrpcConverter(
                new CoreFollowItemToGrpcFollowItemConverter(grpcPeriodMap, userCurrencyCode, categoryCodeById));
        return converter.convertFrom(followItems);
    }

    public FollowItems convertFollowItems(List<se.tink.backend.firehose.v1.models.FollowItem> followItems) {
        return new FirehoseFollowItemToGrpcFollowItemConverter(categoryCodeById).convertFrom(followItems);
    }

    public Periods convertPeriods(List<se.tink.backend.firehose.v1.models.Period> periods) {
        FirehosePeriodToGrpcPeriodConverter periodConverter = new FirehosePeriodToGrpcPeriodConverter();
        return periodConverter.convertFrom(periods);
    }

    public SignableOperations convertSignableOperations(
            List<se.tink.backend.firehose.v1.models.SignableOperation> signableOperations) {
        return new FirehoseSignableOperationToGrpcSignableOperationConverter().convertFrom(signableOperations);
    }

    public StatisticTree convertStatistics(List<Statistic> statistics, UserProfile userProfile) {
        return new FirehoseStatisticsToGrpcStatisticTreeConverter(userProfile, categoryCodeById)
                .convertFrom(statistics);
    }

    public Transactions convertTransactions(List<se.tink.backend.firehose.v1.models.Transaction> transactions) {
        return Transactions.newBuilder().addAllTransaction(
                new FirehoseTransactionToGrpcTransactionConverter(userCurrencyCode, categoryCodeById)
                        .convertFrom(transactions)).build();
    }

    public UserConfiguration convertUserConfiguration(
            se.tink.backend.firehose.v1.models.UserConfiguration userConfiguration) {
        return new FirehoseUserConfigurationToGrpcUserConfigurationConverter().convertFrom(userConfiguration);
    }
}
