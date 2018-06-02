package se.tink.backend.grpc.v1.streaming.context;

import com.google.common.collect.BiMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.core.Account;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.account.AccountsGrpcConverter;
import se.tink.backend.grpc.v1.converter.account.CoreAccountToGrpcAccountConverter;
import se.tink.backend.grpc.v1.converter.category.CoreCategoriesToGrpcCategoryTreeConverter;
import se.tink.backend.grpc.v1.converter.credential.CoreCredentialToGrpcCredentialConverter;
import se.tink.backend.grpc.v1.converter.credential.CredentialsGrpcConverter;
import se.tink.backend.grpc.v1.converter.follow.CoreFollowItemToGrpcFollowItemConverter;
import se.tink.backend.grpc.v1.converter.follow.FollowItemGrpcConverter;
import se.tink.backend.grpc.v1.converter.periods.CorePeriodToGrpcPeriodConverter;
import se.tink.backend.grpc.v1.converter.periods.PeriodsGrpcConverter;
import se.tink.backend.grpc.v1.converter.provider.CoreProviderToGrpcProviderConverter;
import se.tink.backend.grpc.v1.converter.provider.ProvidersGrpcConverter;
import se.tink.backend.grpc.v1.converter.statistic.CoreStatisticsToGrpcStatisticTreeConverter;
import se.tink.backend.grpc.v1.converter.transfer.CoreSignableOperationToGrpcSignableOperationConverter;
import se.tink.backend.grpc.v1.converter.transfer.SignableOperationsGrpcConverter;
import se.tink.backend.grpc.v1.converter.user.CoreUserToUserConfigurationConverter;
import se.tink.backend.grpc.v1.streaming.flags.DynamicFeatureFlagsUpdater;
import se.tink.backend.grpc.v1.utils.ProtobufModelUtils;
import se.tink.backend.main.controllers.AccountServiceController;
import se.tink.backend.main.controllers.CategoryController;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.backend.main.controllers.FollowServiceController;
import se.tink.backend.main.controllers.ProviderServiceController;
import se.tink.backend.main.controllers.StatisticsServiceController;
import se.tink.backend.main.controllers.TransferServiceController;
import se.tink.backend.main.controllers.UserServiceController;
import se.tink.grpc.v1.models.Accounts;
import se.tink.grpc.v1.models.CategoryTree;
import se.tink.grpc.v1.models.Credentials;
import se.tink.grpc.v1.models.FollowItems;
import se.tink.grpc.v1.models.Periods;
import se.tink.grpc.v1.models.Providers;
import se.tink.grpc.v1.models.SignableOperations;
import se.tink.grpc.v1.models.StatisticTree;
import se.tink.grpc.v1.models.UserConfiguration;
import se.tink.grpc.v1.rpc.StreamingResponse;

public class ContextGenerator {
    private final AccountServiceController accountServiceController;
    private final CategoryController categoryController;
    private final CredentialServiceController credentialServiceController;
    private final FollowServiceController followServiceController;
    private final ProviderServiceController providerServiceController;
    private final StatisticsServiceController statisticsServiceController;
    private final UserServiceController userServiceController;
    private final TransferServiceController transferServiceController;
    private final BiMap<String, String> categoryCodeById;
    private final DynamicFeatureFlagsUpdater dynamicFeatureFlagsUpdater;

    @Inject
    public ContextGenerator(AccountServiceController accountServiceController,
            CategoryController categoryController,
            CredentialServiceController credentialServiceController,
            FollowServiceController followServiceController,
            ProviderServiceController providerServiceController,
            StatisticsServiceController statisticsServiceController,
            UserServiceController userServiceController,
            TransferServiceController transferServiceController,
            @Named("categoryCodeById") BiMap<String, String> categoryCodeById,
            DynamicFeatureFlagsUpdater dynamicFeatureFlagsUpdater) {
        this.accountServiceController = accountServiceController;
        this.categoryController = categoryController;
        this.credentialServiceController = credentialServiceController;
        this.followServiceController = followServiceController;
        this.providerServiceController = providerServiceController;
        this.statisticsServiceController = statisticsServiceController;
        this.userServiceController = userServiceController;
        this.transferServiceController = transferServiceController;
        this.categoryCodeById = categoryCodeById;
        this.dynamicFeatureFlagsUpdater = dynamicFeatureFlagsUpdater;
    }

    public StreamingResponse generateContext(User user, Map<String, Provider> providersByCredentialIds)
            throws LockException {
        String userCurrency = user.getProfile().getCurrency();

        AccountsGrpcConverter accountsGrpcConverter = new AccountsGrpcConverter(
                new CoreAccountToGrpcAccountConverter(userCurrency, providersByCredentialIds));

        List<Account> accounts = accountServiceController.list(user.getId());
        Accounts accountsResponse = accountsGrpcConverter.convertFrom(accounts);

        CredentialsGrpcConverter credentialsGrpcConverter = new CredentialsGrpcConverter(
                new CoreCredentialToGrpcCredentialConverter(new Locale(user.getLocale())));
        Credentials credentials = credentialsGrpcConverter.convertFrom(credentialServiceController.list(user));

        StatisticTree statistics = new CoreStatisticsToGrpcStatisticTreeConverter(user.getProfile(), categoryCodeById)
                .convertFrom(statisticsServiceController
                        .getContextStatistics(user.getId(), user.getProfile().getPeriodMode(), false));

        CategoryTree categoryTree = new CoreCategoriesToGrpcCategoryTreeConverter()
                .convertFrom(categoryController.list(user.getLocale()));

        ProvidersGrpcConverter providersGrpcConverter = new ProvidersGrpcConverter(
                new CoreProviderToGrpcProviderConverter());
        Providers providers = providersGrpcConverter
                .convertFrom(providerServiceController.list(user.getId(), user.getProfile().getMarket()));

        List<se.tink.libraries.date.Period> periodsList = userServiceController.getPeriods(user);
        PeriodsGrpcConverter periodConverter = new PeriodsGrpcConverter(new CorePeriodToGrpcPeriodConverter());
        Periods periods = periodConverter.convertFrom(periodsList);

        FollowItemGrpcConverter followItemGrpcConverter = new FollowItemGrpcConverter(
                new CoreFollowItemToGrpcFollowItemConverter(periods.getPeriodMap(), userCurrency,
                        categoryCodeById));
        FollowItems followItems = followItemGrpcConverter.convertFrom(followServiceController.list(user, true, null));

        SignableOperationsGrpcConverter signableOperationsGrpcConverter = new SignableOperationsGrpcConverter(
                new CoreSignableOperationToGrpcSignableOperationConverter());
        SignableOperations signableOperations = signableOperationsGrpcConverter
                .convertFrom(transferServiceController.getMostRecentSignableOperations(user));

        dynamicFeatureFlagsUpdater.updateResidenceTabFlag(user, accounts);

        UserConfiguration userConfiguration = new CoreUserToUserConfigurationConverter().convertFrom(user);

        return StreamingResponse
                .newBuilder()
                .setType(StreamingResponse.Type.READ)
                .setTimestamp(ProtobufModelUtils.getCurrentProtobufTimestamp())
                .setAccounts(accountsResponse)
                .setCredentials(credentials)
                .setStatistics(statistics)
                .setCategories(categoryTree)
                .setProviders(providers)
                .setPeriods(periods)
                .setFollowItems(followItems)
                .setSignableOperations(signableOperations)
                .setUserConfiguration(userConfiguration)
                .build();
    }
}
